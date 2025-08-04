package com.hlt.usermanagement.utils;

import com.hlt.usermanagement.dto.request.UsernameLoginRequest;
import com.hlt.usermanagement.model.RoleModel;
import com.hlt.usermanagement.model.UserModel;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


public class UserBuilderUtil {

    public static UserModel createStandardUser(UsernameLoginRequest request, PasswordEncoder encoder, RoleModel role) {
        UserModel user = new UserModel();

        user.setUsername(request.getUsername());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPrimaryContact(request.getPrimaryContact());
        user.setRecentActivityDate(LocalDate.now());
        user.setLastLogOutDate(LocalDate.now());

        Set<RoleModel> roles = new HashSet<>();
        roles.add(role);
        user.setRoleModels(roles);

        return user;
    }
}
