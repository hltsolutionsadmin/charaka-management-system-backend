package com.hlt.usermanagement.services.impl;

import com.hlt.usermanagement.dto.MailRequestDTO;
import com.hlt.usermanagement.dto.enums.EmailType;
import com.hlt.usermanagement.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendMail(MailRequestDTO request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(buildEmailContent(request.getType(), request.getVariables()), true);

            mailSender.send(message);
            log.info("Email sent to {}", request.getTo());

        } catch (MessagingException e) {
            log.error("Email sending failed to {}: {}", request.getTo(), e.getMessage(), e);
        }
    }

    private String buildEmailContent(EmailType type, java.util.Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        return switch (type) {
            case HOSPITAL_ADMIN_ONBOARD -> templateEngine.process("emails/hospital-admin_onboard", context);
            case DOCTOR_ONBOARD -> templateEngine.process("emails/doctor_onboard", context);
            case RECEPTIONIST_ACCESS -> templateEngine.process("emails/receptionist_access", context);
            case TELECALLER_ACCESS -> templateEngine.process("emails/telecaller_access", context);
            case FORGOT_PASSWORD -> templateEngine.process("emails/forgot_password", context);
        };
    }

}
