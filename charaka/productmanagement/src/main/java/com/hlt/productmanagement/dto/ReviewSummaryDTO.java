package com.hlt.productmanagement.dto;


import com.hlt.productmanagement.dto.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDTO {
    private Long productId;
    private Long businessId;
    private ReviewType type;
    private Double averageRating;
    private Long totalReviews;
}
