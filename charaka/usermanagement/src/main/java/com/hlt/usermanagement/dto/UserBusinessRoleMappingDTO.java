package com.hlt.usermanagement.dto;

import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.dto.response.BusinessAttributeResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
