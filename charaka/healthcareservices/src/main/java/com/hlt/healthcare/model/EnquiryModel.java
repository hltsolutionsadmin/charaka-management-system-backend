package com.hlt.healthcare.model;

import com.hlt.auth.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
        name = "enquiries",
        indexes = {
                @Index(name = "idx_prospect_contact_hash", columnList = "prospect_contact_hash")
        }
)
public class EnquiryModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "telecaller_id", nullable = false)
    private Long telecallerId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "prospect_name")
    private String prospectName;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "prospect_contact")
    private String prospectContact;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "prospect_email")
    private String prospectEmail;

    @Column(name = "prospect_contact_hash", length = 64, nullable = false)
    private String prospectContactHash;

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

    @OneToMany(mappedBy = "enquiry", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AppointmentModel> appointments;
}
