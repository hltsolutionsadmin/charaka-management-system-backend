package com.hlt.usermanagement.controllers;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.dto.UserDTO;
import com.hlt.commonservice.enums.ERole;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import com.hlt.usermanagement.services.UserService;
import com.hlt.utils.JuavaryaConstants;
import com.hlt.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
public class UserBusinessRoleMappingController {

    private final UserService userService;
    private final UserBusinessRoleMappingService userBusinessRoleMappingService;

    @PostMapping("/onboard-hospital-admin")
    @PreAuthorize(JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> onboardHospitalAdmin(
            @RequestBody UserBusinessRoleMappingDTO dto) {
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.onboardHospitalAdmin(dto);
        return ResponseEntity.ok(StandardResponse.single("Hospital Admin onboarded successfully", result));
    }

    @PostMapping("/onboard-doctor")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> onboardDoctor(
            @RequestBody UserBusinessRoleMappingDTO dto) {
        Long resolvedBusinessId = resolveHospitalId(dto.getBusinessId());
        dto.setBusinessId(resolvedBusinessId);
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.onboardDoctor(dto);
        return ResponseEntity.ok(StandardResponse.single("Doctor onboarded successfully", result));
    }

    @PostMapping("/onboard-telecaller")
    @PreAuthorize(JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> onboardTelecaller(
            @RequestBody UserBusinessRoleMappingDTO dto) {
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.onboardTelecaller(dto);
        return ResponseEntity.ok(StandardResponse.single("Telecaller onboarded successfully", result));
    }

    @PostMapping("/onboard-receptionist")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> onboardReceptionist(
            @RequestBody UserBusinessRoleMappingDTO dto) {
        Long resolvedBusinessId = resolveHospitalId(dto.getBusinessId());
        dto.setBusinessId(resolvedBusinessId);
        UserBusinessRoleMappingDTO result = userBusinessRoleMappingService.onboardReceptionist(dto);
        return ResponseEntity.ok(StandardResponse.single("Receptionist onboarded successfully", result));
    }

    @PostMapping("/assign-telecaller")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> assignTelecallerToHospital(
            @RequestParam Long telecallerUserId,
            @RequestParam(required = false) Long hospitalId) {
        Long resolvedBusinessId = resolveHospitalId(hospitalId);
        UserBusinessRoleMappingDTO result =
                userBusinessRoleMappingService.assignTelecallerToHospital(telecallerUserId, resolvedBusinessId);
        return ResponseEntity.ok(StandardResponse.single("Telecaller assigned successfully", result));
    }

    @GetMapping("/doctors")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<Page<UserDTO>>> getDoctorsByHospital(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long hospitalId) {
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
            @RequestParam(required = false) Long hospitalId) {
        Long resolvedHospitalId = resolveHospitalId(hospitalId);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> receptionists =
                userBusinessRoleMappingService.getReceptionistsByHospital(resolvedHospitalId, pageable);
        return ResponseEntity.ok(StandardResponse.page("Receptionists for hospital", receptionists));
    }

    @GetMapping("/available-telecallers")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<Page<UserDTO>>> getAvailableTelecallersForAssignment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long hospitalId) {
        Long resolvedHospitalId = resolveHospitalId(hospitalId);
        Page<UserDTO> telecallers =
                userBusinessRoleMappingService.getAssignableTelecallersForHospital(resolvedHospitalId, page, size);
        return ResponseEntity.ok(StandardResponse.page("Available telecallers for assignment", telecallers));
    }


    private UserDTO fetchCurrentUser() {
        UserDetailsImpl userDetails = SecurityUtils.getCurrentUserDetails();
        return userService.getUserById(userDetails.getId());

    }

    /**
     * Resolve hospitalId based on role:
     * - SuperAdmin → must explicitly pass hospitalId
     * - HospitalAdmin → auto-resolved from assigned hospital
     */
    private Long resolveHospitalId(Long inputHospitalId) {
        UserDTO currentUser = fetchCurrentUser();  // now returns UserDTO

        // Check if Super Admin
        boolean isSuperAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> ERole.ROLE_SUPER_ADMIN.equals(role.getName()));

        if (isSuperAdmin) {
            if (inputHospitalId == null) {
                throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "Super Admin must provide hospitalId");
            }
            return inputHospitalId;
        }

        // Hospital Admin case
        List<Map<String, Object>> businesses = currentUser.getBusinesses();
        if (businesses == null || businesses.isEmpty()) {
            throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "Has no associated businesses");
        }
        if (businesses.size() > 1) {
            throw new HltCustomerException(ErrorCode.BAD_REQUEST, "Multiple hospitals found. Please specify hospitalId");
        }

        // Get the only hospital ID from the first map
        Map<String, Object> business = businesses.get(0);
        Object idObj = business.get("id");
        if (idObj == null) {
            throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND, "Hospital ID not found");
        }

        return Long.valueOf(idObj.toString());
    }

    @GetMapping("/partners")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN + " or " + JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<Page<UserDTO>>> getPartnersByBusinessAndType(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long hospitalId) {

        Long resolvedHospitalId = resolveHospitalId(hospitalId);

        Page<UserDTO> partners = userBusinessRoleMappingService.getPartnersByBusinessAndType(
                resolvedHospitalId, type, page, size);

        return ResponseEntity.ok(StandardResponse.page("Partners for hospital and type", partners));
    }


}



