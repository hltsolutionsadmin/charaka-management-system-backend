package com.juvarya.user.access.mgmt.populator;

import com.juvarya.user.access.mgmt.dto.AddressDTO;
import com.juvarya.user.access.mgmt.dto.UserDTO;
import com.juvarya.user.access.mgmt.model.ProjectLoginFlags;
import com.juvarya.user.access.mgmt.model.UserModel;
import com.juvarya.utils.Populator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        target.setType(source.getType());
        target.setBranch(source.getBranch());
        target.setProfilePicture(source.getProfilePicture());
        target.setFcmToken(source.getFcmToken());
        target.setJuviId(source.getJuviId());
        target.setLastLogOutDate(source.getLastLogOutDate());
        target.setRecentActivityDate(source.getRecentActivityDate());
        target.setRollNumber(source.getRollNumber());
        target.setQualification(source.getQualification());

        ProjectLoginFlags flags = source.getProjectLoginFlags();
        if (flags != null) {
            target.setSkillrat(Boolean.TRUE.equals(flags.getSkillrat()));
            target.setYardly(Boolean.TRUE.equals(flags.getYardly()));
            target.setEato(Boolean.TRUE.equals(flags.getEato()));
            target.setSancharalakshmi(Boolean.TRUE.equals(flags.getSancharalakshmi()));
        } else {
            target.setSkillrat(false);
            target.setYardly(false);
            target.setEato(false);
            target.setSancharalakshmi(false);
        }

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
    }
}
