package com.juvarya.user.access.mgmt.services;


import com.juvarya.commonservice.enums.ERole;
import com.juvarya.user.access.mgmt.model.RoleModel;

public interface RoleService {
    RoleModel findByErole(ERole eRole);
}
