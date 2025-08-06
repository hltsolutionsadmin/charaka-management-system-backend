package com.hlt.usermanagement.dto;

import com.hlt.commonservice.enums.ERole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBusinessRoleMappingDTO {

    private Long mappingId;
    private Long businessId;
    private String businessName;
    private ERole role;
    private boolean active;
    private UserDTO userDetails;

}
