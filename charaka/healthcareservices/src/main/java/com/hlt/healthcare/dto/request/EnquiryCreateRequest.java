package com.hlt.healthcare.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EnquiryCreateRequest {

    private Long businessId;

    private Long telecallerId;

    private Long patientId;

    private String prospectName;

    private String prospectContact;

    private String prospectEmail;

    private String enquiryReason;

    private String interactionNotes;

    private LocalDate nextFollowUpDate;

    private Boolean followUpDone = false;

    private Boolean convertedToAppointment = false;

    private Boolean convertedToPatient = false;
}
