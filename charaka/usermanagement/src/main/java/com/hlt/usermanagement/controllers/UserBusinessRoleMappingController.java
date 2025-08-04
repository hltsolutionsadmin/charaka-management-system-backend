package com.hlt.usermanagement.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.services.UserBusinessRoleMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mappings")
@RequiredArgsConstructor
public class UserBusinessRoleMappingController {

    private final UserBusinessRoleMappingService mappingService;

    @PostMapping("/assign")
    public ResponseEntity<StandardResponse<UserBusinessRoleMappingDTO>> assignUserToBusiness(
            @RequestBody UserDTO userDTO,
            @RequestParam Long b2bUnitId,
            @RequestParam String role
    ) {
        UserBusinessRoleMappingDTO dto = mappingService.assignUserToBusinessWithUserDetails(userDTO, b2bUnitId, role);
        return ResponseEntity.ok(StandardResponse.single("User assigned successfully", dto));
    }



    @DeleteMapping("/{mappingId}")
    public ResponseEntity<StandardResponse<String>> deactivateMapping(
            @PathVariable Long mappingId
    ) {
        mappingService.deactivateMapping(mappingId);
        return ResponseEntity.ok(StandardResponse.message("Mapping deactivated successfully"));
    }
}
