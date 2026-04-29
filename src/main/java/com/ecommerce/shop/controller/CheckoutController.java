package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.CheckoutRequest;
import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.CheckoutResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Checkout and payment session APIs")
@SecurityRequirement(name = "bearerAuth")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    @Operation(summary = "Create Stripe checkout session",
            description = "Creates a payment session from cart and returns Stripe hosted checkout URL.")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            Authentication authentication,
            @RequestBody(required = false) CheckoutRequest request
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Checkout session created!",
                        checkoutService.createCheckout(user, request)
                )
        );
    }
}
