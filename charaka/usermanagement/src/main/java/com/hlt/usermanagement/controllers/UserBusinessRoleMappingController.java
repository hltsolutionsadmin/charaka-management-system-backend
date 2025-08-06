package com.hlt.usermanagement.controllers;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;
import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.enums.ERole;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.model.UserModel;
import com.hlt.usermanagement.repository.UserRepository;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import com.hlt.utils.JuavaryaConstants;
import com.hlt.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
public class UserBusinessRoleMappingController {

    private final UserRepository userRepository;
    private final UserBusinessRoleMappingService userBusinessRoleMappingService;

    @PostMapping("/onboard-hospital-admin")
    @PreAuthorize(JuavaryaConstants.ROLE_SUPER_ADMIN)
    public ResponseEntity<StandardResponse<String>> onboardHospitalAdmin(@RequestBody UserBusinessRoleMappingDTO dto) {
        userBusinessRoleMappingService.onboardHospitalAdmin(dto);
        return ResponseEntity.ok(StandardResponse.message("Hospital Admin onboarded successfully"));
    }

    @PostMapping("/onboard-doctor")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN)
    public ResponseEntity<StandardResponse<String>> onboardDoctor(@RequestBody UserBusinessRoleMappingDTO dto) {
        UserModel currentUser = fetchCurrentUser();
        validateHospitalAdminAccess(currentUser);
        dto.setBusinessId(currentUser.getB2bUnit().getId());
        userBusinessRoleMappingService.onboardDoctor(dto);
        return ResponseEntity.ok(StandardResponse.message("Doctor onboarded successfully"));
    }

    @PostMapping("/onboard-receptionist")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN)
    public ResponseEntity<StandardResponse<String>> onboardReceptionist(@RequestBody UserBusinessRoleMappingDTO dto) {
        UserModel currentUser = fetchCurrentUser();
        validateHospitalAdminAccess(currentUser);
        dto.setBusinessId(currentUser.getB2bUnit().getId());
        userBusinessRoleMappingService.onboardReceptionist(dto);
        return ResponseEntity.ok(StandardResponse.message("Receptionist onboarded successfully"));
    }

    @PostMapping("/assign-telecaller")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN)
    public ResponseEntity<StandardResponse<String>> assignTelecallerToHospital(@RequestParam Long telecallerUserId) {
        UserModel currentUser = fetchCurrentUser();
        validateHospitalAdminAccess(currentUser);
        Long businessId = currentUser.getB2bUnit().getId();
        userBusinessRoleMappingService.assignTelecallerToHospital(telecallerUserId, businessId);
        return ResponseEntity.ok(StandardResponse.message("Telecaller assigned successfully to your hospital"));
    }

    @GetMapping("/available-telecallers")
    @PreAuthorize(JuavaryaConstants.ROLE_HOSPITAL_ADMIN)
    public ResponseEntity<StandardResponse<List<UserDTO>>> getAvailableTelecallersForAssignment() {
        UserModel currentUser = fetchCurrentUser();
        validateHospitalAdminAccess(currentUser);
        Long hospitalId = currentUser.getB2bUnit().getId();
        List<UserDTO> telecallers = userBusinessRoleMappingService.getAssignableTelecallersForHospital(hospitalId);
        return ResponseEntity.ok(StandardResponse.list("Available telecallers for assignment", telecallers));
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

        if (user.getB2bUnit() == null) {
            throw new HltCustomerException(ErrorCode.BUSINESS_NOT_FOUND);
        }
    }

}
