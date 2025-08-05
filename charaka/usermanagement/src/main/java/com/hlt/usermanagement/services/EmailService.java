package com.hlt.usermanagement.services;

public interface EmailService {
    void sendUserCredentials(String to, String username, String password);
}
