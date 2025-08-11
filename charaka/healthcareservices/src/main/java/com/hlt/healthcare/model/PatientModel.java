package com.hlt.healthcare.model;

import com.hlt.auth.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "patients",
        indexes = {
                @Index(name = "idx_patient_business_id", columnList = "business_id"),
                @Index(name = "idx_patient_appointment_id", columnList = "appointment_id")
        })
public class PatientModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "registered_by")
    private Long registeredBy;

    @Column(name = "patient_code", unique = true, length = 50)
    private String patientCode;

    @Column(name = "status", length = 20)
    private String status;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private AppointmentModel appointment;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
