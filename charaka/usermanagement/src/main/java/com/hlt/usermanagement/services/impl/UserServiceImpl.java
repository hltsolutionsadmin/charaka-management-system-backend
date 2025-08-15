package com.hlt.usermanagement.services.impl;

import com.hlt.usermanagement.model.*;
import com.hlt.auth.UserServiceAdapter;
import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.B2BUnitDTO;
import com.hlt.commonservice.dto.BasicOnboardUserDTO;
import com.hlt.commonservice.dto.MediaDTO;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.commonservice.enums.ERole;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.usermanagement.dto.UserUpdateDTO;
import com.hlt.usermanagement.repository.*;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import com.hlt.usermanagement.services.UserService;
import com.hlt.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

@Service
@Slf4j
public class UserServiceImpl implements UserService, UserServiceAdapter {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CaffeineCacheManager cacheManager;
    @Autowired private MediaRepository mediaRepository;
    @Autowired private B2BUnitRepository b2bUnitRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired
    private UserBusinessRoleMappingRepository mappingRepository;
    @Override
    public UserModel saveUser(UserModel userModel) {
        try {
            return userRepository.save(userModel);
        } catch (Exception ex) {
            log.error("Failed to save user: {}", userModel, ex);
            throw ex; // Re-throw or wrap in custom exception
        }
    }


    @Override
    public Long onBoardUserWithCredentials(BasicOnboardUserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS);
        }

        UserModel user = new UserModel();
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setPrimaryContact(dto.getPrimaryContact());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoleModels(fetchRoles(dto.getUserRoles()));

        if (dto.getBusinessId() != null) {
            B2BUnitModel business = findB2BUnitById(dto.getBusinessId());
            Set<B2BUnitModel> businesses = new HashSet<>();
            businesses.add(business);
            user.setBusinesses(businesses);
        }

        return saveUser(user).getId();
    }

    @Override
    public void updateUser(UserUpdateDTO details, Long userId) {
        UserDetailsImpl currentUser = SecurityUtils.getCurrentUserDetails();
        UserModel user = getUserByIdOrThrow(currentUser.getId());

        if (!Objects.equals(userId, user.getId())) {
            throw new HltCustomerException(ErrorCode.USER_NOT_FOUND);
        }

        if (existsByEmail(details.getEmail(), userId)) {
            throw new HltCustomerException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        user.setEmail(details.getEmail());
        user.setFullName(details.getFullName());
        saveUser(user); // handles cache internally
    }

    @Override
    public Long onBoardUser(String fullName, String mobileNumber, Set<ERole> userRoles, Long b2bUnitId) {
        Optional<UserModel> existingUserOpt = findByPrimaryContact(mobileNumber);
        if (existingUserOpt.isPresent()) {
            return existingUserOpt.get().getId();
        }

        B2BUnitModel b2bUnit = b2bUnitId != null ? findB2BUnitById(b2bUnitId) : null;

        UserModel user = new UserModel();
        user.setPrimaryContact(mobileNumber);
        user.setRoleModels(fetchRoles(userRoles));
        user.setCreationTime(new Date());
        user.setFullName(fullName);

        if (b2bUnit != null) {
            Set<B2BUnitModel> businesses = new HashSet<>();
            businesses.add(b2bUnit);
            user.setBusinesses(businesses);
        }

        return saveUser(user).getId();
    }


    @Override
    public void addUserRole(Long userId, ERole userRole) {
        UserModel user = getUserByIdOrThrow(userId);
        RoleModel role = getRoleByEnum(userRole);

        if (user.getRoleModels().add(role)) {
            saveUser(user);
        }
    }

    @Override
    public void removeUserRole(String mobileNumber, ERole userRole) {
        UserModel user = findByPrimaryContact(mobileNumber)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
        RoleModel role = getRoleByEnum(userRole);

        if (!user.getRoleModels().remove(role)) {
            throw new HltCustomerException(ErrorCode.ROLE_NOT_FOUND);
        }

        saveUser(user);
    }

    @Override
    @Transactional
    public UserDTO getUserById(Long userId) {
        UserModel user = getUserByIdOrThrow(userId);
        UserDTO dto = convertToUserDto(user);

        List<MediaDTO> mediaList = mediaRepository.findByCustomerId(userId)
                .stream()
                .map(this::convertToMediaDto)
                .toList();
        dto.setMedia(mediaList);

        return dto;
    }
    private MediaDTO convertToMediaDto(MediaModel media) {
        MediaDTO dto = new MediaDTO();
        dto.setId(media.getId());
        dto.setUrl(media.getUrl());
        dto.setName(media.getFileName());
        dto.setDescription(media.getDescription());
        dto.setExtension(media.getExtension());
        dto.setCreationTime(media.getCreationTime());
        dto.setMediaType(media.getMediaType());
        return dto;
    }

    @Override
    public UserModel findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public List<UserModel> findByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    @Override
    public UserModel findByEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }

        String emailHash = DigestUtils.sha256Hex(email.trim().toLowerCase());
        return userRepository.findByEmailHash(emailHash).orElse(null);
    }


    @Override
    public Optional<UserModel> findByPrimaryContact(String primaryContact) {
        return userRepository.findByPrimaryContactHash(DigestUtils.sha256Hex(primaryContact));
    }
    @Override
    public Optional<UserDTO> findDtoByPrimaryContact(String primaryContact) {
        return userRepository.findByPrimaryContact(primaryContact)
                .map(this::convertToUserDto);
    }

    @Override
    public List<UserDTO> getUsersByRole(String roleName) {
        ERole role = ERole.valueOf(roleName.toUpperCase());
        RoleModel roleModel = getRoleByEnum(role);

        return userRepository.findByRoleModelsContaining(roleModel)
                .stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public void clearFcmToken(Long userId) {
        UserModel user = getUserByIdOrThrow(userId);
        user.setFcmToken(null);
        userRepository.save(user);
    }

    @Override
    public long getUserCountByBusinessId(Long businessId) {
        return userRepository.countUsersByBusinessId(businessId);
    }

    @Override
    public Optional<UserModel> findByUsername(@NotBlank String username) {
        return userRepository.findByUsername(username);
    }
    private void updateCache(Long userId, UserModel userModel) {
        UserDTO dto = convertToUserDto(userModel);
        Cache userCache = cacheManager.getCache("users");
        if (userCache != null) {
            userCache.put(userId, dto);
        }
    }

    private RoleModel getRoleByEnum(ERole role) {
        return roleRepository.findByName(role)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.ROLE_NOT_FOUND));
    }

    private B2BUnitModel findB2BUnitById(Long id) {
        return b2bUnitRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND));
    }

    private Set<RoleModel> fetchRoles(Set<ERole> roles) {
        return roleRepository.findByNameIn(roles);
    }

    private UserModel getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
    }

    public UserDTO convertToUserDto(UserModel user) {
        // Map roles
        Set<com.hlt.commonservice.dto.Role> roles = Optional.ofNullable(user.getRoleModels())
                .orElse(Collections.emptySet())
                .stream()
                .map(role -> new com.hlt.commonservice.dto.Role(role.getId(), role.getName()))
                .collect(Collectors.toSet());

        // Get profile picture URL
        String profilePicture = getProfilePictureUrl(user.getId());

        List<Long> mappedBusinessIds = mappingRepository.findByUserId(user.getId())
                .stream()
                .map(mapping -> mapping.getB2bUnit().getId())
                .distinct()
                .toList();

        List<Long> businessIds = user.getBusinesses() != null
                ? user.getBusinesses().stream()
                .map(B2BUnitModel::getId)
                .toList()
                : Collections.emptyList();


        Set<Long> allBusinessIds = new HashSet<>();
        allBusinessIds.addAll(businessIds);
        allBusinessIds.addAll(mappedBusinessIds);

        Map<String, String> userAttributes = user.getAttributes()
                .stream()
                .collect(Collectors.toMap(UserAttributeModel::getAttributeName, UserAttributeModel::getAttributeValue));

        return UserDTO.builder()
                .id(user.getId())
                .fullName(StringUtils.trimToNull(user.getFullName()))
                .primaryContact(StringUtils.trimToNull(user.getPrimaryContact()))
                .email(StringUtils.trimToNull(user.getEmail()))
                .creationTime(user.getCreationTime())
                .token(user.getFcmToken())
                .username(StringUtils.trimToNull(user.getUsername()))
                .gender(StringUtils.trimToNull(user.getGender()))
                .profilePicture(profilePicture)
                .roles(roles)
                .juviId(StringUtils.trimToNull(user.getJuviId()))
                .attributes(userAttributes)
                .businessIds(new ArrayList<>(allBusinessIds))
                .build();
    }

    private String getProfilePictureUrl(Long userId) {
        MediaModel profilePicture = mediaRepository.findByCustomerIdAndMediaType(userId, "PROFILE_PICTURE");
        return profilePicture != null ? profilePicture.getUrl() : null;
    }

    private B2BUnitDTO convertToB2BDTO(B2BUnitModel unit) {
        B2BUnitDTO dto = new B2BUnitDTO();
        dto.setId(unit.getId());
        dto.setBusinessName(unit.getBusinessName());
        dto.setEnabled(unit.isEnabled());
        return dto;
    }

    public Boolean existsByEmail(String email, Long userId) {
        return userRepository.existsByEmailAndNotUserId(email, userId);
    }
}


