package com.hlt.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnquiryDTO {

    private Long id;
    private Long businessId;
    private Long telecallerId;
    private String prospectName;
    private String prospectContact;
    private String prospectEmail;
    private String enquiryReason;
    private String interactionNotes;
    private LocalDate nextFollowUpDate;
    private Boolean followUpDone;
    private Boolean convertedToAppointment;
    private Boolean convertedToPatient;
    private List<AppointmentDTO> appointments;
    private Instant creationTime;
    private Instant modificationTime;
    private String createdByName;
}
