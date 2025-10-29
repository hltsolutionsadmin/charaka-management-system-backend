package com.juvarya.order.dto.request;

import com.juvarya.order.dto.enums.ComplaintType;
import lombok.Data;

@Data
public class ComplaintCreateRequest {
    private String title;
    private String description;
    private Long createdBy;
    private Long userId;
    private ComplaintType complaintType;
    private String orderId;
    private Long businessId;
}
