package com.hlt.healthcare.model;

import com.hlt.healthcare.dto.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "appointments")
public class AppointmentModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private EnquiryModel enquiry;

    @OneToOne(mappedBy = "appointment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private PatientModel patient;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(name = "appointment_notes", columnDefinition = "TEXT")
    private String appointmentNotes;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
}
