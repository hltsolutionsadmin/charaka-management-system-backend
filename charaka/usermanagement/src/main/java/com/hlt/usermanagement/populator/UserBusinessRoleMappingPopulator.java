package com.hlt.usermanagement.populator;

import com.hlt.commonservice.dto.UserDTO;
import com.hlt.usermanagement.dto.UserBusinessRoleMappingDTO;
import com.hlt.usermanagement.model.UserBusinessRoleMappingModel;
import com.hlt.usermanagement.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class UserBusinessRoleMappingPopulator {

    private final UserPopulator userPopulator;
    private final UserService userService;

    public void populate(UserBusinessRoleMappingModel source, UserBusinessRoleMappingDTO target) {
        if (source == null || target == null) return;

        target.setMappingId(source.getId());

        if (source.getB2bUnit() != null) {
            target.setBusinessName(source.getB2bUnit().getBusinessName());
        }

        target.setRole(source.getRole());
        target.setActive(Boolean.TRUE.equals(source.getIsActive()));

        if (source.getUser() != null) {
            UserDTO userDTO = userService.convertToUserDto(source.getUser());
            target.setUserDetails(userDTO);
        }
    }
}