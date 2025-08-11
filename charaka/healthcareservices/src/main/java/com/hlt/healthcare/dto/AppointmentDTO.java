package com.hlt.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hlt.healthcare.dto.enums.AppointmentStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentDTO {

    private Long id;

    private Long hospitalId;

    private Long enquiryId;

    private Long doctorId;

    private LocalDateTime appointmentDateTime;

    private String appointmentNotes;

    private AppointmentStatus status;

    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;

}
