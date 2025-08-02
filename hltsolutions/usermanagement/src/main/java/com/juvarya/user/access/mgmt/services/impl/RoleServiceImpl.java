package com.juvarya.user.access.mgmt.services.impl;


import com.juvarya.commonservice.enums.ERole;
import com.juvarya.user.access.mgmt.model.RoleModel;
import com.juvarya.user.access.mgmt.repository.RoleRepository;
import com.juvarya.user.access.mgmt.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public RoleModel findByErole(ERole eRole) {
        Optional<RoleModel> role = roleRepository.findByName(eRole);
        return role.orElse(null);
    }
}
