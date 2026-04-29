package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.ReviewRequest;
import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.ReviewResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Product Reviews", description = "Product review and rating management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a review for a product")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody @Valid ReviewRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Review submitted!", reviewService.submitReview(user, productId, request)));
    }

    @GetMapping
    @Operation(summary = "Get all reviews for a product")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched!", reviewService.getProductReviews(productId)));
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update a review")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            Authentication authentication,
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Review updated!", reviewService.updateReview(user, reviewId, request)));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            Authentication authentication,
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        User user = (User) authentication.getPrincipal();
        reviewService.deleteReview(user, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted!", null));
    }
}
