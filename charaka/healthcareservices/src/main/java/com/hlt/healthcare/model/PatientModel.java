package com.hlt.healthcare.model;

import jakarta.persistence.*;

@Entity
@Table(name = "patient")
public class PatientModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId; // from user-management

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "allergies")
    private String allergies;

    @Column(name = "emergency_contact")
    private String emergencyContact;
}
