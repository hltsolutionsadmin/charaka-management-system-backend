package com.hlt.healthcare.model;
import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "doctor")
public class DoctorModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // from user-management

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "specialization")
    private String specialization;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "consultation_fee")
    private Double consultationFee;
}
