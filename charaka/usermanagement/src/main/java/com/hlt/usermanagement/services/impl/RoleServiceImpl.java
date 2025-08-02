package com.hlt.usermanagement.services.impl;


import com.hlt.commonservice.enums.ERole;
import com.hlt.usermanagement.model.RoleModel;
import com.hlt.usermanagement.repository.RoleRepository;
import com.hlt.usermanagement.services.RoleService;
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
