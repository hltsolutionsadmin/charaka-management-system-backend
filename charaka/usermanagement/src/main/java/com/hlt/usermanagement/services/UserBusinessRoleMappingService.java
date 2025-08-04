package com.hlt.usermanagement.services;

import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;

public interface UserBusinessRoleMappingService {

    void deactivateMapping(Long mappingId);

    UserBusinessRoleMappingDTO assignUserToBusinessWithUserDetails(UserDTO userDTO, Long b2bUnitId, String role);

}
