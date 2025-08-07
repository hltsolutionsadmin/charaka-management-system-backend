package com.hlt.healthcare.model;

import com.hlt.healthcare.dto.enums.AppointmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
public class AppointmentModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "appointment_time")
    private LocalDateTime appointmentTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
