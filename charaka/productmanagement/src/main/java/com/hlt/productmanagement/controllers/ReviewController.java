package com.hlt.productmanagement.controllers;

import com.hlt.commonservice.dto.StandardResponse;
import com.hlt.commonservice.user.UserDetailsImpl;
import com.hlt.productmanagement.dto.ReviewDTO;
import com.hlt.productmanagement.dto.ReviewSummaryDTO;
import com.hlt.productmanagement.service.ReviewService;
import com.hlt.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<StandardResponse<ReviewDTO>> submit(@RequestBody ReviewDTO dto) {
        UserDetailsImpl loggedInUser = SecurityUtils.getCurrentUserDetails();
        dto.setUserId(loggedInUser.getId());
        ReviewDTO saved = reviewService.submitReview(dto);
        return ResponseEntity.ok(StandardResponse.single("Review submitted successfully", saved));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<StandardResponse<Page<ReviewDTO>>> productReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewDTO> reviews = reviewService.getReviewsForProduct(productId, pageable);
        return ResponseEntity.ok(StandardResponse.page("Fetched product reviews", reviews));
    }

    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<StandardResponse<ReviewSummaryDTO>> productSummary(@PathVariable Long productId) {
        ReviewSummaryDTO summary = reviewService.getSummaryForProduct(productId);
        return ResponseEntity.ok(StandardResponse.single("Fetched product review summary", summary));
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<StandardResponse<Page<ReviewDTO>>> businessReviews(
            @PathVariable Long businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewDTO> reviews = reviewService.getReviewsForBusiness(businessId, pageable);
        return ResponseEntity.ok(StandardResponse.page("Fetched business reviews", reviews));
    }

    @GetMapping("/business/{businessId}/summary")
    public ResponseEntity<StandardResponse<ReviewSummaryDTO>> businessSummary(@PathVariable Long businessId) {
        ReviewSummaryDTO summary = reviewService.getSummaryForBusiness(businessId);
        return ResponseEntity.ok(StandardResponse.single("Fetched business review summary", summary));
    }
}
