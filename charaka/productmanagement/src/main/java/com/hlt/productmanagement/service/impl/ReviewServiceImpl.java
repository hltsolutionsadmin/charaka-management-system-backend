package com.hlt.productmanagement.service.impl;

import com.hlt.auth.exception.handling.ErrorCode;
import com.hlt.auth.exception.handling.HltCustomerException;

import com.hlt.productmanagement.client.OrderClient;
import com.hlt.productmanagement.dto.ReviewDTO;
import com.hlt.productmanagement.dto.ReviewSummaryDTO;
import com.hlt.productmanagement.dto.enums.ReviewType;
import com.hlt.productmanagement.model.ReviewModel;
import com.hlt.productmanagement.repository.ReviewRepository;
import com.hlt.productmanagement.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderClient orderClient;

    @Override
    @Transactional
    public ReviewDTO submitReview(ReviewDTO dto) {
        if (!isEligibleToReview(dto)) {
            throw new HltCustomerException(ErrorCode.UNAUTHORIZED_REVIEW);
        }

        ReviewModel model = buildReviewModel(dto);
        reviewRepository.save(model);
        return buildReviewDTO(model);
    }

    @Override
    public Page<ReviewDTO> getReviewsForProduct(Long productId, Pageable pageable) {
        return reviewRepository
                .findByProductIdAndType(productId, ReviewType.PRODUCT, pageable)
                .map(this::buildReviewDTO);
    }

    @Override
    public Page<ReviewDTO> getReviewsForBusiness(Long businessId, Pageable pageable) {
        return reviewRepository
                .findByBusinessIdAndType(businessId, ReviewType.BUSINESS, pageable)
                .map(this::buildReviewDTO);
    }

    @Override
    public ReviewSummaryDTO getSummaryForProduct(Long productId) {
        return buildReviewSummary(productId, null, ReviewType.PRODUCT);
    }

    @Override
    public ReviewSummaryDTO getSummaryForBusiness(Long businessId) {
        return buildReviewSummary(null, businessId, ReviewType.BUSINESS);
    }

    private boolean isEligibleToReview(ReviewDTO dto) {
        return switch (dto.getType()) {
            case PRODUCT -> orderClient.hasUserOrderedProductFromBusiness(dto.getUserId(), dto.getProductId(), dto.getBusinessId());
            case BUSINESS -> orderClient.hasUserOrderedFromBusiness(dto.getUserId(), dto.getBusinessId());
        };
    }

    private ReviewModel buildReviewModel(ReviewDTO dto) {
        return ReviewModel.builder()
                .userId(dto.getUserId())
                .productId(dto.getProductId())
                .businessId(dto.getBusinessId())
                .type(dto.getType())
                .rating(dto.getRating())
                .comment(dto.getComment())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ReviewDTO buildReviewDTO(ReviewModel model) {
        return ReviewDTO.builder()
                .userId(model.getUserId())
                .productId(model.getProductId())
                .businessId(model.getBusinessId())
                .type(model.getType())
                .rating(model.getRating())
                .comment(model.getComment())
                .createdAt(model.getCreatedAt())
                .build();
    }

    private ReviewSummaryDTO buildReviewSummary(Long productId, Long businessId, ReviewType type) {
        Double avg;
        Long count;

        if (type == ReviewType.PRODUCT) {
            avg = reviewRepository.averageRatingForProduct(productId);
            count = reviewRepository.countByProductIdAndType(productId, type);
        } else {
            avg = reviewRepository.averageRatingForBusiness(businessId);
            count = reviewRepository.countByBusinessIdAndType(businessId, type);
        }

        return ReviewSummaryDTO.builder()
                .productId(productId)
                .businessId(businessId)
                .type(type)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .totalReviews(count)
                .build();
    }
}
