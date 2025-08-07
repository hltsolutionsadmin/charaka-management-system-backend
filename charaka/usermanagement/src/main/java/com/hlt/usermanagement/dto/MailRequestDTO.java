package com.hlt.usermanagement.dto;

import com.hlt.usermanagement.dto.enums.EmailType;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailRequestDTO {
    private String to;
    private String subject;
    private EmailType type;
    private Map<String, Object> variables;
}
