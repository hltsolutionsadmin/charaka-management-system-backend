package com.hlt.usermanagement.services.impl;

import com.hlt.usermanagement.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Set;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Async
    @Override
    public void sendUserCredentials(String to, String username, String password, String businessName, Set<String> roles) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("password", password);
        context.setVariable("businessName", businessName);
        context.setVariable("roles", roles);

        String body = templateEngine.process("user-credentials.html", context);

        MimeMessagePreparator message = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject("Your Account Credentials");
            helper.setText(body, true);
        };

        mailSender.send(message);
    }

}
