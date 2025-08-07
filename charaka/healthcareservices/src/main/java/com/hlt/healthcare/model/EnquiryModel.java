package com.hlt.healthcare.model;

import com.hlt.healthcare.dto.enums.EnquiryStatus;
import com.hlt.healthcare.dto.enums.EnquiryType;
import jakarta.persistence.*;

@Entity
@Table(name = "enquiry")
public class EnquiryModel extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caller_user_id", nullable = false)
    private Long callerUserId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(name = "telecaller_interaction_id")
    private Long telecallerInteractionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "enquiry_type")
    private EnquiryType enquiryType;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "enquiry_status")
    private EnquiryStatus enquiryStatus;
}
