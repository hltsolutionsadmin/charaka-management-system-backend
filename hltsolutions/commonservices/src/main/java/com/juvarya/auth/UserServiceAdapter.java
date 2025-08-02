package com.juvarya.auth;

import com.juvarya.commonservice.dto.UserDTO;

public interface UserServiceAdapter {
    UserDTO getUserById(Long userId);
}
