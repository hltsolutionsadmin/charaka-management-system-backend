package com.hlt.usermanagement.services;


import com.hlt.usermanagement.dto.MailRequestDTO;

import java.util.Map;

public interface EmailService {
    void sendMail(MailRequestDTO request);


}
