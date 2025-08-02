package com.juvarya.delivery.client;


import com.juvarya.auth.UserServiceAdapter;
import com.juvarya.commonservice.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceAdapterImpl implements UserServiceAdapter {

    @Autowired
    private UserMgmtClient userMgmtClient;

    @Override
    public UserDTO getUserById(Long userId) {
        return userMgmtClient.getUserById(userId);
    }
}
