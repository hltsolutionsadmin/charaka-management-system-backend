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
            case WELCOME_EMAIL -> templateEngine.process("emails/welcome", context);
            case PASSWORD_GENERATION -> templateEngine.process("emails/password-generation", context);
            default -> templateEngine.process("emails/generic", context);
        };
    }
}
