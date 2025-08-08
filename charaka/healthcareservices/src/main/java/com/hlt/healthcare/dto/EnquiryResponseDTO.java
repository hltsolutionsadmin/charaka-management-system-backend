package com.hlt.healthcare.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class EnquiryResponseDTO {

    private Long id;
    private Long businessId;

    private Long telecallerId;
    private String telecallerName;

    private Long patientId;
    private String patientName;

    private String prospectName;
    private String prospectContact;
    private String prospectEmail;

    private String enquiryReason;
    private String interactionNotes;

    private LocalDate nextFollowUpDate;
    private Boolean followUpDone;
    private Boolean convertedToAppointment;
    private Boolean convertedToPatient;
}
