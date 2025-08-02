package com.juvarya.user.access.mgmt.services.impl;

import com.juvarya.auth.UserServiceAdapter;
import com.juvarya.auth.exception.handling.ErrorCode;
import com.juvarya.auth.exception.handling.JuvaryaCustomerException;
import com.juvarya.commonservice.dto.B2BUnitDTO;
import com.juvarya.commonservice.dto.BasicOnboardUserDTO;
import com.juvarya.commonservice.dto.MediaDTO;
import com.juvarya.commonservice.dto.UserDTO;
import com.juvarya.commonservice.enums.ERole;
import com.juvarya.commonservice.enums.UserVerificationStatus;
import com.juvarya.commonservice.user.UserDetailsImpl;
import com.juvarya.user.access.mgmt.dto.UserUpdateDTO;
import com.juvarya.user.access.mgmt.model.*;
import com.juvarya.user.access.mgmt.repository.B2BUnitRepository;
import com.juvarya.user.access.mgmt.repository.MediaRepository;
import com.juvarya.user.access.mgmt.repository.RoleRepository;
import com.juvarya.user.access.mgmt.repository.UserRepository;
import com.juvarya.user.access.mgmt.services.UserService;
import com.juvarya.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
@Slf4j
public class UserServiceImpl implements UserService, UserServiceAdapter {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private CaffeineCacheManager cacheManager;
    @Autowired private MediaRepository mediaRepository;
    @Autowired private B2BUnitRepository b2bUnitRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public UserModel saveUser(UserModel userModel) {
        UserModel saved = userRepository.save(userModel);
        updateCache(saved.getId(), saved);
        return saved;
    }

    @Override
    public Long onBoardUserWithCredentials(BasicOnboardUserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new JuvaryaCustomerException(ErrorCode.USER_ALREADY_EXISTS);
        }

        B2BUnitModel business = dto.getBusinessId() != null ? findB2BUnitById(dto.getBusinessId()) : null;

        UserModel user = new UserModel();
        user.setUsername(dto.getUsername());
        user.setFullName(dto.getFullName());
        user.setPrimaryContact(dto.getPrimaryContact());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoleModels(fetchRoles(dto.getUserRoles()));
        user.setB2bUnit(business);

        return saveUser(user).getId();
    }

    @Override
    public void updateUser(UserUpdateDTO details, Long userId) {
        UserDetailsImpl currentUser = SecurityUtils.getCurrentUserDetails();
        UserModel user = getUserByIdOrThrow(currentUser.getId());

        if (!Objects.equals(userId, user.getId())) {
            throw new JuvaryaCustomerException(ErrorCode.USER_NOT_FOUND);
        }

        if (existsByEmail(details.getEmail(), userId)) {
            throw new JuvaryaCustomerException(ErrorCode.EMAIL_ALREADY_IN_USE);
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
        user.setB2bUnit(b2bUnit);

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
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.USER_NOT_FOUND));
        RoleModel role = getRoleByEnum(userRole);

        if (!user.getRoleModels().remove(role)) {
            throw new JuvaryaCustomerException(ErrorCode.ROLE_NOT_FOUND);
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
        dto.setUserVerificationStatus(user.getUserVerificationStatus());

        return dto;
    }

    @Override
    @Transactional
    public void verifyStudent(Long userId, UserVerificationStatus status) {
        UserModel user = getUserByIdOrThrow(userId);
        user.setUserVerificationStatus(status);

        if (status == UserVerificationStatus.VERIFIED) {
            RoleModel studentRole = getRoleByEnum(ERole.ROLE_STUDENT);
            user.getRoleModels().add(studentRole);
        }

        saveUser(user);
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
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public Optional<UserModel> findByPrimaryContact(String primaryContact) {
        return userRepository.findByPrimaryContact(primaryContact);
    }

    @Override
    public Page<UserModel> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
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
    public Page<UserDTO> getUnverifiedStudents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserModel> userPage = userRepository.findAllUsersByRoleAndVerificationStatus(UserVerificationStatus.NOT_VERIFIED, pageable);
        return userPage.map(this::convertToUserDto);
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
    @Transactional
    public void deleteRolesAndResetSkillratFlag(Long userId) {
        UserModel user = getUserByIdOrThrow(userId);
        user.getRoleModels().clear();
        if (user.getAddresses() != null) {
            user.getAddresses().clear();
        }
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void approveDeliveryPartner(Long userId) {
        UserModel user = getUserByIdOrThrow(userId);
        ProjectLoginFlags flags = Optional.ofNullable(user.getProjectLoginFlags())
                .orElseGet(() -> {
                    ProjectLoginFlags newFlags = new ProjectLoginFlags();
                    user.setProjectLoginFlags(newFlags);
                    return newFlags;
                });

        flags.setDeliveryPartner(true);
        userRepository.save(user);
    }

    @Override
    public List<UserDTO> getUsersByRoleAndDeliveryPartnerFalse(ERole role) {
        return userRepository.findByRoleAndDeliveryPartnerFlagFalse(role)
                .stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
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
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.ROLE_NOT_FOUND));
    }

    private B2BUnitModel findB2BUnitById(Long id) {
        return b2bUnitRepository.findById(id)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.BUSINESS_NOT_FOUND));
    }

    private Set<RoleModel> fetchRoles(Set<ERole> roles) {
        return roleRepository.findByNameIn(roles);
    }

    private UserModel getUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new JuvaryaCustomerException(ErrorCode.USER_NOT_FOUND));
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

    public UserDTO convertToUserDto(UserModel user) {
        Set<com.juvarya.commonservice.dto.Role> roles = user.getRoleModels().stream()
                .map(role -> new com.juvarya.commonservice.dto.Role(role.getId(), role.getName()))
                .collect(Collectors.toSet());

        String profilePicture = Optional.ofNullable(
                        mediaRepository.findByCustomerIdAndMediaType(user.getId(), "PROFILE_PICTURE"))
                .map(MediaModel::getUrl)
                .orElse(null);

        B2BUnitDTO b2bUnit = Optional.ofNullable(user.getB2bUnit())
                .map(this::convertToB2BDTO)
                .orElseGet(() -> b2bUnitRepository.findByUserModel(user)
                        .map(this::convertToB2BDTO)
                        .orElse(null));

        ProjectLoginFlags flags = Optional.ofNullable(user.getProjectLoginFlags()).orElse(new ProjectLoginFlags());

        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .primaryContact(user.getPrimaryContact())
                .email(user.getEmail())
                .branch(user.getBranch())
                .creationTime(user.getCreationTime())
                .token(user.getFcmToken())
                .username(user.getUsername())
                .gender(user.getGender())
                .profilePicture(profilePicture)
                .type(user.getType())
                .roles(roles)
                .juviId(user.getJuviId())
                .skillrat(Boolean.TRUE.equals(flags.getSkillrat()))
                .yardly(Boolean.TRUE.equals(flags.getYardly()))
                .eato(Boolean.TRUE.equals(flags.getEato()))
                .sancharalakshmi(Boolean.TRUE.equals(flags.getSancharalakshmi()))
                .deliveryPartner(Boolean.TRUE.equals(flags.getDeliveryPartner()))
                .currentYear(user.getCurrentYear())
                .studentStartYear(user.getStudentStartYear())
                .studentEndYear(user.getStudentEndYear())
                .password(user.getPassword())
                .b2bUnit(b2bUnit)
                .build();
    }

    private B2BUnitDTO convertToB2BDTO(B2BUnitModel unit) {
        B2BUnitDTO dto = new B2BUnitDTO();
        dto.setId(unit.getId());
        dto.setBusinessName(unit.getBusinessName());
        dto.setApproved(unit.isApproved());
        dto.setEnabled(unit.isEnabled());
        return dto;
    }

    public Boolean existsByEmail(String email, Long userId) {
        return userRepository.existsByEmailAndNotUserId(email, userId);
    }
}


