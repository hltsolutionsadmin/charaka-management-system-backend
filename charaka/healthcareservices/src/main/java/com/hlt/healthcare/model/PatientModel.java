package com.hlt.healthcare.model;


import com.hlt.auth.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "patients")
public class PatientModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private AppointmentModel appointment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id")
    private EnquiryModel enquiry;

    @Column(name = "registered_by")
    private Long registeredBy;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "email")
    private String email;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "gender")
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
