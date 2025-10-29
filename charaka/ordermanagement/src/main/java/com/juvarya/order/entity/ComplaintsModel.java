package com.juvarya.order.entity;

import com.juvarya.order.dto.enums.ComplaintStatus;
import com.juvarya.order.dto.enums.ComplaintType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "COMPLAINTS")
public class ComplaintsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @Column(name = "created_by")
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ComplaintStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type")
    private ComplaintType complaintType;

    @Column(name = "assigned_to")
    private Long assignedTo;

    @Column(name = "assigned_on")
    private LocalDateTime assignedOn;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "b2b_id")
    private Long b2bId;

}
