package com.juvarya.user.access.mgmt.services.impl;


import com.juvarya.user.access.mgmt.model.UserModel;
import com.juvarya.user.access.mgmt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl {
    @Autowired
    UserRepository userRepository;

    @Transactional
    public UserModel loadUserByPrimaryContact(String primaryContact) {
        try {
            Optional<UserModel> optionalUser = userRepository.findByPrimaryContact(primaryContact);
            return optionalUser.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }


}
