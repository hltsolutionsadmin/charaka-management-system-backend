package com.hlt.healthcare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientCreateRequest {

    @NotNull(message = "Enquiry ID is required")
    private Long enquiryId;

    @NotNull(message = "Business ID is required")
    private Long businessId;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotBlank(message = "Primary contact is required")
    private String primaryContact;

    private String secondaryContact;

    private String email;

    private LocalDate dateOfBirth;

    private String gender;

    private String notes;

}
