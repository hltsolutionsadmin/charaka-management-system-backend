package com.hlt.usermanagement.populator;

import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import org.springframework.stereotype.Component;

@Component
public class UserBusinessRoleMappingPopulator {

    public void populate(UserBusinessRoleMappingModel source, UserBusinessRoleMappingDTO target) {
        target.setId(source.getId());
        target.setUserId(source.getUser().getId());
        target.setB2bUnitId(source.getB2bUnit().getId());
        target.setRole(source.getRole());
        target.setActive(source.isActive());
    }
}
