package com.hlt.healthcare.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientResponseDTO {

    private Long id;
    private Long businessId;

    private Long enquiryId;
    private Long appointmentId;

    private String fullName;
    private String contactNumber;
    private String email;
    private String gender;
    private LocalDate dob;

    private String notes;

    private Long registeredBy;
    private String registeredByName;
}
