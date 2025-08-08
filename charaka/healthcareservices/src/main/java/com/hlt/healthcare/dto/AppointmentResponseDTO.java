package com.hlt.healthcare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentResponseDTO {

    private Long id;
    private Long businessId;

    private Long enquiryId;

    private Long doctorId;
    private String doctorName;

    private LocalDateTime appointmentDateTime;
    private String appointmentNotes;
    private String status;

    private Long patientId;
    private String patientName;
}
