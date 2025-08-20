package com.hlt.usermanagement.populator;

import com.hlt.usermanagement.dto.AddressDTO;
import com.hlt.usermanagement.dto.UserDTO;
import com.hlt.usermanagement.model.UserAttributeModel;
import com.hlt.usermanagement.model.UserModel;
import com.hlt.utils.Populator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserPopulator implements Populator<UserModel, UserDTO> {

    @Autowired
    private AddressPopulator addressPopulator;

    @Override
    public void populate(UserModel source, UserDTO target) {
        populate(source, target, true);
    }

    public void populate(UserModel source, UserDTO target, boolean includeAddresses) {
        if (source == null || target == null) {
            return;
        }

        target.setId(source.getId());
        target.setFullName(source.getFullName());
        target.setUsername(source.getUsername());
        target.setEmail(source.getEmail());
        target.setPrimaryContact(source.getPrimaryContact());
        target.setGender(source.getGender());
        target.setFcmToken(source.getFcmToken());
        target.setJuviId(source.getJuviId());
        target.setLastLogOutDate(source.getLastLogOutDate());
        target.setRecentActivityDate(source.getRecentActivityDate());
        target.setBusinessIds(Collections.singletonList(source.getBusinesses() != null && !source.getBusinesses().isEmpty()
                ? source.getBusinesses().iterator().next().getId()
                : null));

        if (includeAddresses && source.getAddresses() != null && !source.getAddresses().isEmpty()) {
            target.setAddresses(
                    source.getAddresses().stream().map(address -> {
                        AddressDTO dto = new AddressDTO();
                        addressPopulator.populate(address, dto);
                        return dto;
                    }).collect(Collectors.toList())
            );
        }

        if (source.getRoleModels() != null && !source.getRoleModels().isEmpty()) {
            target.setRoles(
                    source.getRoleModels().stream()
                            .map(role -> role.getName().name())
                            .collect(Collectors.toSet())
            );
        }

        // Populate dynamic attributes
        if (source.getAttributes() != null && !source.getAttributes().isEmpty()) {
            Map<String, String> attributesMap = source.getAttributes().stream()
                    .collect(Collectors.toMap(UserAttributeModel::getAttributeName,
                            UserAttributeModel::getAttributeValue));
            target.setAttributes(attributesMap);
        }
    }

    public UserDTO toDTO(UserModel source) {
        if (source == null) return null;
        UserDTO dto = new UserDTO();
        populate(source, dto);
        return dto;
    }
}
