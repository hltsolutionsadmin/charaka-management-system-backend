package com.hlt.usermanagement.services;


import com.hlt.usermanagement.dto.MailRequestDTO;

public interface EmailService {
    void sendMail(MailRequestDTO request);
}
