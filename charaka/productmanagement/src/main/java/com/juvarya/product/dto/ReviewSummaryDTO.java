package com.juvarya.product.dto;


import com.juvarya.product.dto.enums.ReviewType;
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
