package com.hlt.usermanagement.controllers;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.*;
import com.hlt.commonservice.enums.ERole;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.usermanagement.azure.service.AwsBlobService;
import com.hlt.usermanagement.dto.BasicUserDetails;
import com.hlt.usermanagement.dto.UserUpdateDTO;
import com.hlt.usermanagement.model.MediaModel;
import com.hlt.usermanagement.model.UserModel;
import com.hlt.usermanagement.populator.MediaPopulator;
import com.hlt.usermanagement.repository.B2BUnitRepository;
import com.hlt.usermanagement.repository.UserRepository;
import com.hlt.usermanagement.services.MediaService;
import com.hlt.usermanagement.services.UserService;
import com.hlt.utils.AbstractConverter;
import com.hlt.utils.JTBaseEndpoint;
import com.hlt.utils.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/user")
@Slf4j
public class UserEndpoint extends JTBaseEndpoint {
    @Autowired
    private UserService userService;

    @Autowired
    private AwsBlobService awsBlobService;

    @Autowired
    private MediaPopulator mediaPopulator;

    @Autowired
    private B2BUnitRepository b2bUnitRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private MediaService mediaService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/find/{userId}")
    public UserDTO getUserById(@PathVariable("userId") Long userId) {
        log.info("Request received to fetch customer details for ID: {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping("/userDetails")
    public UserDTO getUserByToken() {
        UserDetailsImpl user = SecurityUtils.getCurrentUserDetails();
        Long userId = user.getId();
        log.info("Request received to fetch customer details for ID: {}", userId);
        return userService.getUserById(userId);
    }

    @PostMapping("/details/all")
    public List<UserModel> getUserDetailsByIds(@RequestBody List<Long> userIds) {
        return userService.findByIds(userIds);
    }
    @GetMapping("/by-contact/{contact}")
    public UserDTO getUserByPrimaryContact(@Valid @PathVariable String primaryContact) {
        return userService.findDtoByPrimaryContact(primaryContact)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
    }
    @PutMapping("/user/role/{role}")
    public ResponseEntity<?> assignRoleToCurrentUser(@PathVariable String role) {
        UserDetailsImpl currentUser = SecurityUtils.getCurrentUserDetails();

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Long userId = currentUser.getId();

        try {
            ERole parsedRole = ERole.valueOf(role);
            userService.addUserRole(userId, parsedRole);

            return ResponseEntity.ok().body(
                    Map.of(
                            "message", "RoleModel successfully added",
                            "role", parsedRole.name()
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid role: " + role)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Failed to add role: " + e.getMessage())
            );
        }
    }


    @PutMapping(value = "/userDetails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StandardResponse<String>> updateBasicUserDetails(
            @ModelAttribute @Valid BasicUserDetails details) throws IOException {

        log.info("Entering into Update Basic UserModel Details API");

        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();
        UserModel userModel = userService.findById(loggedInUser.getId());

        if (userModel == null) {
            log.error("UserModel not found with ID: {}", loggedInUser.getId());
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND);
        }

        log.info("Updating details for userModel with ID: {}", loggedInUser.getId());

        Optional.ofNullable(details.getFullName()).filter(StringUtils::hasText).ifPresent(userModel::setFullName);

        Optional.ofNullable(details.getEmail())
                .filter(StringUtils::hasText)
                .ifPresent(email -> {
                    if (!userService.existsByEmail(email, userModel.getId())) {
                        userModel.setEmail(email);
                    } else {
                        log.warn("Email '{}' is already in use", email);
                        throw new HltCustomerException(ErrorCode.EMAIL_ALREADY_IN_USE);
                    }
                });

        Optional.ofNullable(details.getFcmToken()).ifPresent(userModel::setFcmToken);


        Long userId = loggedInUser.getId();
        List<MediaModel> mediaModels = new ArrayList<>();

        if (details.getProfilePicture() != null && !details.getProfilePicture().isEmpty()) {
            MediaModel profilePicMedia = awsBlobService.uploadCustomerPictureFile(
                    userId, details.getProfilePicture(), userId);

            String originalFilename = details.getProfilePicture().getOriginalFilename();
            profilePicMedia.setFileName(originalFilename);
            profilePicMedia.setName(originalFilename);

            profilePicMedia.setMediaType("PROFILE_PICTURE");
            profilePicMedia.setCustomerId(userId);
            profilePicMedia.setCreatedBy(userId);
            profilePicMedia.setCreationTime(new Date());
            profilePicMedia.setModificationTime(new Date());

            mediaService.saveMedia(profilePicMedia);
        }

        if (details.getMediaFiles() != null && !details.getMediaFiles().isEmpty()) {
            for (MultipartFile file : details.getMediaFiles()) {
                if (!file.isEmpty()) {
                    MediaModel uploadedMedia = awsBlobService.uploadFile(file);
                    String originalFilename = file.getOriginalFilename();

                    uploadedMedia.setFileName(originalFilename);
                    uploadedMedia.setName(originalFilename);
                    uploadedMedia.setMediaType("USER_PROFILE");
                    uploadedMedia.setCustomerId(userId);
                    uploadedMedia.setCreatedBy(userId);
                    uploadedMedia.setCreationTime(new Date());
                    uploadedMedia.setModificationTime(new Date());

                    mediaService.saveMedia(uploadedMedia);
                    mediaModels.add(uploadedMedia);
                }
            }
        }

        if (details.getMediaUrls() != null) {
            for (String url : details.getMediaUrls()) {
                if (StringUtils.hasText(url)) {
                    MediaModel media = new MediaModel();
                    media.setUrl(url);
                    media.setFileName("external");
                    media.setExtension("url");
                    media.setMediaType("EXTERNAL_LINK");
                    media.setName("External URL: " + url);
                    media.setCreationTime(new Date());
                    media.setModificationTime(new Date());
                    media.setCreatedBy(userId);
                    media.setCustomerId(userId);

                    mediaService.saveMedia(media);
                    mediaModels.add(media);
                }
            }
        }

        if (details.getMedia() != null) {
            for (MediaDTO dto : details.getMedia()) {
                MediaModel media = new MediaModel();
                media.setUrl(dto.getUrl());
                media.setFileName(dto.getName());
                media.setExtension(dto.getExtension());
                media.setMediaType(dto.getMediaType());
                media.setCreationTime(dto.getCreationTime() != null ? dto.getCreationTime() : new Date());
                media.setModificationTime(new Date());
                media.setCreatedBy(userId);
                media.setCustomerId(userId);
                media.setName(StringUtils.hasText(dto.getName()) ? dto.getName() : dto.getUrl());
                media.setDescription(dto.getDescription());

                mediaService.saveMedia(media);
                mediaModels.add(media);
            }
        }

        userService.saveUser(userModel);

        log.info("UserModel and media details updated successfully for ID: {}", userId);
        return ResponseEntity.ok(
                StandardResponse.single("User details updated successfully", "SUCCESS")
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity updateUser(@PathVariable("id") @Valid @NotBlank Long id, @Valid @RequestBody UserUpdateDTO details) {
        log.info("Entering into Update Basic UserModel Details API");
        userService.updateUser(details, id);
        log.info("UserModel details updated successfully");
        return ResponseEntity.ok().body(new MessageResponse("Details Updated"));
    }

    @PostMapping("/onboard/user")
    public Long onBoardUser(@Valid @RequestBody BasicOnboardUserDTO basicOnboardUserDTO) {
        return userService.onBoardUser(basicOnboardUserDTO.getFullName(), basicOnboardUserDTO.getPrimaryContact(),
                basicOnboardUserDTO.getUserRoles(), basicOnboardUserDTO.getBusinessId());
    }

    @PostMapping("/onboard-with-credentials")
    public ResponseEntity<Long> onBoardUserWithCredentials(@Valid @RequestBody BasicOnboardUserDTO dto) {
        Long userId = userService.onBoardUserWithCredentials(dto);
        return ResponseEntity.ok((userId));
    }


    @PostMapping("/save")
    public UserModel saveUser(@RequestBody UserModel userModel) {
        return userService.saveUser(userModel);
    }

    @DeleteMapping("/contact/{mobileNumber}/role/{role}")
    public ResponseEntity<?> removeUserRole(@PathVariable("mobileNumber") String mobileNumber,
                                            @PathVariable("role") ERole userRole) {
        try {
            userService.removeUserRole(mobileNumber, userRole);

            return ResponseEntity.ok().body(Collections.singletonMap("message", "RoleModel successfully removed from user"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Failed to remove role: " + e.getMessage()));
        }
    }


    @GetMapping("/contact")
    public LoggedInUser getByPrimaryContact(@Valid @RequestParam("primaryContact") String primaryContact) {
        log.info("Request received to fetch customer details for primary Contact: {}", primaryContact);
        Optional<UserModel> customer = userService.findByPrimaryContact(primaryContact);
        if (customer.isPresent()) {
            UserModel userModel = customer.get();
            LoggedInUser loggedInUser = new LoggedInUser();
            loggedInUser.setId(userModel.getId());
            loggedInUser.setFullName(userModel.getFullName());
            List<String> roles = userModel.getRoleModels().stream().map(role -> role.getName().name())
                    .collect(Collectors.toList());
            loggedInUser.setRoles(new HashSet<>(roles));
            loggedInUser.setPrimaryContact(userModel.getPrimaryContact());

            log.info("Customer details fetched successfully for Primary Contact: {}", primaryContact);
            return loggedInUser;
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    @PostMapping("/profile/upload")
    public ResponseEntity<MediaDTO> uploadCustomerProfilePicture(@ModelAttribute MultipartFile profilePicture)
            throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        log.info("Entering uploadCustomerProfilePicture API");
        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();

        if (!ObjectUtils.isEmpty(loggedInUser)) {
            MediaModel mediaModel = awsBlobService.uploadCustomerPictureFile(loggedInUser.getId(), profilePicture,
                    loggedInUser.getId());
            MediaDTO mediaDTO = (MediaDTO) getConvertedInstance().convert(mediaModel);
            log.info("Profile picture uploaded successfully for user with ID {}", loggedInUser.getId());
            return new ResponseEntity<>(mediaDTO, HttpStatus.OK);
        }
        log.error("LoggedInUser not found or invalid");
        return new ResponseEntity<>(new MediaDTO(), HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings("unchecked")
    public AbstractConverter getConvertedInstance() {
        return getConverter(mediaPopulator, MediaDTO.class.getName());
    }

    @GetMapping("/byRole")
    public List<UserDTO> getUsersByRole(@RequestParam String roleName) {
        return userService.getUsersByRole(roleName);
    }

    @DeleteMapping("/fcmToken")
    public ResponseEntity<?> deleteFcmToken() {
        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();
        userService.clearFcmToken(loggedInUser.getId());
        return ResponseEntity.ok(new MessageResponse("FCM token deleted successfully"));
    }

    @GetMapping("/count/business/{businessId}")
    public ResponseEntity<StandardResponse<Long>> getUserCountByBusinessId(@PathVariable Long businessId) {
        long count = userService.getUserCountByBusinessId(businessId);
        return ResponseEntity.ok(StandardResponse.single("User count fetched successfully", count));
    }
}
