package com.hlt.healthcare.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import com.hlt.healthcare.dto.AppointmentDTO;

import java.util.Objects;

@Getter
@Setter
public class PatientDTO {

    private Long id;

    private Long businessId;

    private Long registeredBy;

    private String patientCode;

    private String status;

    private String emergencyContact;

    private String bloodGroup;

    private String allergies;

    private AppointmentDTO appointment;

    private LocalDate dob;

    private String notes;

    private Instant creationTime;

    private Instant modificationTime;

    private String createdBy;

    private String updatedBy;

    private Instant createdAt;

    private Instant updatedAt;

}
