package com.hlt.usermanagement.services;

import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import java.util.List;

public interface UserBusinessRoleMappingService {

    UserBusinessRoleMappingDTO assignUserToBusiness(Long userId, Long b2bUnitId, String role);

    List<UserBusinessRoleMappingDTO> getUserRoles(Long userId);

    void deactivateMapping(Long mappingId);
}
