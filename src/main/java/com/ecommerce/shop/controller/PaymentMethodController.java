package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.PaymentMethodResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.service.PaymentMethodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Tag(name = "Payment Methods", description = "Authenticated user payment method management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    @Operation(summary = "Get saved payment methods (Cards)")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getMyPaymentMethods(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Payment methods fetched!", paymentMethodService.getMyPaymentMethods(user)));
    }

    @DeleteMapping("/{paymentMethodId}")
    @Operation(summary = "Remove a saved payment method")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(
            Authentication authentication,
            @PathVariable String paymentMethodId) {
        User user = (User) authentication.getPrincipal();
        paymentMethodService.deletePaymentMethod(user, paymentMethodId);
        return ResponseEntity.ok(ApiResponse.success("Payment method removed!", null));
    }
}
