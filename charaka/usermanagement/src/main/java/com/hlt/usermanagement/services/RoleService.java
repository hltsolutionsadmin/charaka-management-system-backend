package com.hlt.usermanagement.services;


import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.model.RoleModel;

public interface RoleService {
    RoleModel findByErole(ERole eRole);
}
