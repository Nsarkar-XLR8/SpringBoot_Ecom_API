package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.request.ReviewRequest;
import com.ecommerce.shop.dto.response.ReviewResponse;
import com.ecommerce.shop.entity.Product;
import com.ecommerce.shop.entity.Review;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.ProductRepository;
import com.ecommerce.shop.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ReviewResponse submitReview(User user, Long productId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found!"));

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .product(product)
                .build();

        return mapToReviewResponse(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found!");
        }
        return reviewRepository.findAllByProductId(productId).stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse updateReview(User user, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or not owned by user!"));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return mapToReviewResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or not owned by user!"));
        reviewRepository.delete(review);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
