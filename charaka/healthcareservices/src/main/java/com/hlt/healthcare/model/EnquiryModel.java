package com.hlt.healthcare.model;


import com.hlt.auth.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "enquiries")
public class EnquiryModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "telecaller_id", nullable = false)
    private Long telecallerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private PatientModel patient;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "prospect_name")
    private String prospectName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "prospect_contact")
    private String prospectContact;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "prospect_email")
    private String prospectEmail;

    @Column(name = "enquiry_reason")
    private String enquiryReason;

    @Column(name = "interaction_notes", columnDefinition = "TEXT")
    private String interactionNotes;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    @Column(name = "follow_up_done")
    private Boolean followUpDone = false;

    @Column(name = "converted_to_appointment")
    private Boolean convertedToAppointment = false;

    @Column(name = "converted_to_patient")
    private Boolean convertedToPatient = false;
}
