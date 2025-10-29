package com.juvarya.order.dto;

import com.juvarya.order.dto.enums.ComplaintStatus;
import com.juvarya.order.dto.enums.ComplaintType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComplaintDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdDt;
    private Long createdBy;
    private ComplaintStatus status;
    private ComplaintType complaintType;
    private Long assignedTo;
    private LocalDateTime assignedOn;
    private String orderId;
    private Long businessId;
}
