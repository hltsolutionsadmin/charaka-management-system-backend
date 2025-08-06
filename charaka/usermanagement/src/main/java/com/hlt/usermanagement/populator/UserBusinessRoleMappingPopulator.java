package com.hlt.usermanagement.populator;

import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserBusinessRoleMappingPopulator {

    private final UserPopulator userPopulator;

    public void populate(UserBusinessRoleMappingModel source, UserBusinessRoleMappingDTO target) {
        if (source == null || target == null) return;

        target.setMappingId(source.getId());

        if (source.getB2bUnit() != null) {
            target.setBusinessName(source.getB2bUnit().getBusinessName());
        }

        target.setRole(source.getRole());
        target.setActive(Boolean.TRUE.equals(source.getIsActive()));

        if (source.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userPopulator.populate(source.getUser(), userDTO);
            target.setUserDetails(userDTO);
        }
    }

}