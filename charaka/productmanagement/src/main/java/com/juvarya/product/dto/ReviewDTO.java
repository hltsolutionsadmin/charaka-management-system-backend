package com.juvarya.product.dto;

import com.juvarya.product.dto.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long userId;
    private Long productId;
    private Long businessId;
    private ReviewType type;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
