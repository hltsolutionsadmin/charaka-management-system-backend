package com.hlt.usermanagement.controllers;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.enums.ERole;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.model.B2BUnitModel;
import com.hlt.usermanagement.model.UserModel;
import com.hlt.usermanagement.repository.UserRepository;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import com.hlt.utils.JuavaryaConstants;
import com.hlt.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
public class UserBusinessRoleMappingController {

    private final UserRepository userRepository;
    private final UserBusinessRoleMappingService userBusinessRoleMappingService;
    @PostMapping("/onboard-hospital-admin")
    @PreAuthorize(JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> onboardHospitalAdmin(@RequestBody UserBusinessRoleMappingDTO dto) {
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.onboardHospitalAdmin(dto);
        return ResponseEntity.ok(StandardResponse.single("Hospital Admin onboarded successfully", result));
    }

    @PostMapping("/onboard-doctor")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> onboardDoctor(@RequestBody UserBusinessRoleMappingDTO dto) {
        UserModel currentUser = fetchCurrentUser();
        enforceBusinessScope(currentUser, dto);
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.onboardDoctor(dto);
        return ResponseEntity.ok(StandardResponse.single("Doctor onboarded successfully", result));
    }

    @PostMapping("/onboard-telecaller")
    @PreAuthorize(JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> onboardTelecaller(
            @RequestBody UserBusinessRoleMappingDTO dto) {
        UserModel currentUser = fetchCurrentUser();
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.onboardTelecaller(dto);
        return ResponseEntity.ok(StandardResponse.single("Telecaller onboarded successfully", result));
    }

    @PostMapping("/onboard-receptionist")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> onboardReceptionist(@RequestBody UserBusinessRoleMappingDTO dto) {
        UserModel currentUser = fetchCurrentUser();
        enforceBusinessScope(currentUser, dto);
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.onboardReceptionist(dto);
        return ResponseEntity.ok(StandardResponse.single("Receptionist onboarded successfully", result));
    }

    @PostMapping("/assign-telecaller")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> assignTelecallerToHospital(@RequestParam Long telecallerUserId) {
        UserModel currentUser = fetchCurrentUser();
        Long businessId = getBusinessScope(currentUser);
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.assignTelecallerToHospital(telecallerUserId, businessId);
        return ResponseEntity.ok(StandardResponse.single("Telecaller assigned successfully", result));
    }



    @GetMapping("/doctors")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<Page<UserDTO>>> getDoctorsByHospital(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long hospitalId
    ) {
        Long resolvedHospitalId = resolveHospitalId(hospitalId);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> doctors = userBusinessRoleMappingService.getDoctorsByHospital(resolvedHospitalId, pageable);
        return ResponseEntity.ok(StandardResponse.page("Doctors for hospital", doctors));
    }

    @GetMapping("/receptionists")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<Page<UserDTO>>> getReceptionistsByHospital(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long hospitalId
    ) {
        Long resolvedHospitalId = resolveHospitalId(hospitalId);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> receptionists = userBusinessRoleMappingService.getReceptionistsByHospital(resolvedHospitalId, pageable);
        return ResponseEntity.ok(StandardResponse.page("Receptionists for hospital", receptionists));
    }

    @GetMapping("/available-telecallers")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<Page<UserDTO>>> getAvailableTelecallersForAssignment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long hospitalId
    ) {
        Long resolvedHospitalId = resolveHospitalId(hospitalId);
        Page<UserDTO> telecallers = userBusinessRoleMappingService
                .getAssignableTelecallersForHospital(resolvedHospitalId, page, size);
        return ResponseEntity.ok(StandardResponse.page("Available telecallers for assignment", telecallers));
    }


    private UserModel fetchCurrentUser() {
        UserDetailsImpl userDetails = SecurityUtils.getCurrentUserDetails();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateHospitalAdminAccess(UserModel user) {
        boolean isHospitalAdmin = user.getRoleModels().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_HOSPITAL_ADMIN);

        if (!isHospitalAdmin) {
            throw new HltCustomerException(ErrorCode.UNAUTHORIZED);
        }

        if (user.getBusinesses() == null) {
            throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND);
        }
    }
    private void enforceBusinessScope(UserModel currentUser, UserBusinessRoleMappingDTO dto) {
        boolean isSuperAdmin = currentUser.getRoleModels().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_SUPER_ADMIN);

        if (isSuperAdmin) {
            if (dto.getBusinessId() == null) {
                throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "Business ID is required for Super Admin");
            }
            return;
        }

        validateHospitalAdminAccess(currentUser);

        // Assuming currentUser.getBusinesses() returns a Set<B2BUnitModel>
        Set<B2BUnitModel> businesses = currentUser.getBusinesses();
        if (businesses == null || businesses.isEmpty()) {
            throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "User does not have an associated business");
        }

        if (businesses.size() > 1) {
            throw new HltCustomerException(ErrorCode.BAD_REQUEST, "User associated with multiple businesses, specify businessId explicitly");
        }

        // Only one business present - set its ID in the DTO
        Long businessId = businesses.iterator().next().getId();
        dto.setBusinessId(businessId);
    }

    private Long getBusinessScope(UserModel currentUser) {
        boolean isSuperAdmin = currentUser.getRoleModels().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_SUPER_ADMIN);

        if (isSuperAdmin) {
            throw new HltCustomerException(ErrorCode.BAD_REQUEST, "Super admin must provide businessId explicitly");
        }

        validateHospitalAdminAccess(currentUser);

        Set<B2BUnitModel> businesses = currentUser.getBusinesses();
        if (businesses == null || businesses.isEmpty()) {
            throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "User does not have an associated business");
        }

        if (businesses.size() > 1) {
            throw new HltCustomerException(ErrorCode.BAD_REQUEST, "User associated with multiple businesses, specify businessId explicitly");
        }

        return businesses.iterator().next().getId();
    }

    private Long resolveHospitalId(Long inputHospitalId) {
        UserModel currentUser = fetchCurrentUser();
        boolean isSuperAdmin = currentUser.getRoleModels().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_SUPER_ADMIN);

        if (isSuperAdmin) {
            if (inputHospitalId == null) {
                throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "Super Admin must provide hospitalId");
            }
            return inputHospitalId;
        }

        validateHospitalAdminAccess(currentUser);

        Set<B2BUnitModel> businesses = currentUser.getBusinesses();
        if (businesses == null || businesses.isEmpty()) {
            throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "User does not have an associated hospital");
        }

        if (businesses.size() > 1) {
            throw new HltCustomerException(ErrorCode.BAD_REQUEST, "User associated with multiple hospitals, specify hospitalId explicitly");
        }

        return businesses.iterator().next().getId();
    }

}
