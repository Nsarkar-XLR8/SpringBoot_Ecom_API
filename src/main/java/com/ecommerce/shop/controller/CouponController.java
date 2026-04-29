package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.CouponValidationRequest;
import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.CouponResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "User-facing coupon validation and application APIs")
@SecurityRequirement(name = "bearerAuth")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    @Operation(summary = "Validate a coupon code for the current user's cart")
    public ResponseEntity<ApiResponse<CouponResponse>> validateCoupon(
            Authentication authentication,
            @RequestBody @Valid CouponValidationRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Coupon validated!", couponService.validateCoupon(user, request.getCode())));
    }
}
