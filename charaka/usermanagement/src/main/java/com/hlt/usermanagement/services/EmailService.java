package com.hlt.usermanagement.services;

import java.util.Set;

public interface EmailService {
    void sendUserCredentials(String to, String username, String password, String businessName, Set<String> roles);
}
