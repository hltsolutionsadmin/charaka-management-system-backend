package com.hlt.usermanagement.dto;

import com.hlt.commonservice.enums.ERole;
import lombok.Data;

@Data
public class UserBusinessRoleMappingDTO {
    private Long id;
    private Long b2bUnitId;
    private ERole role;
    private boolean isActive;
    private UserDTO userDTO;
}
