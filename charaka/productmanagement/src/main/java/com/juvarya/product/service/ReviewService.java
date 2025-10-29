package com.juvarya.product.service;

import com.juvarya.product.dto.ReviewDTO;
import com.juvarya.product.dto.ReviewSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewDTO submitReview(ReviewDTO dto);
    Page<ReviewDTO> getReviewsForProduct(Long productId, Pageable pageable);
    Page<ReviewDTO> getReviewsForBusiness(Long businessId, Pageable pageable);
    ReviewSummaryDTO getSummaryForProduct(Long productId);
    ReviewSummaryDTO getSummaryForBusiness(Long businessId);
}
