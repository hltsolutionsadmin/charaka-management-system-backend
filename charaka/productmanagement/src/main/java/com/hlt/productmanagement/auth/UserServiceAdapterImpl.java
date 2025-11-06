package com.hlt.productmanagement.auth;

import com.hlt.auth.UserServiceAdapter;
import com.hlt.commonservice.dto.UserDTO;

public class UserServiceAdapterImpl implements UserServiceAdapter {
    @Override
    public UserDTO getUserById(Long userId) {
        // TODO: Integrate with usermanagement service via Feign to fetch real user
        return null;
    }
}
