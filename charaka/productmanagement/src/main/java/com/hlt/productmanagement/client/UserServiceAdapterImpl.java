package com.hlt.productmanagement.client;



import com.hlt.auth.UserServiceAdapter;
import com.hlt.commonservice.dto.UserDTO;
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
