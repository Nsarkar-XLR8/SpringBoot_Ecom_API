package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.service.StripeWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Hidden
public class StripeWebhookController {

    private final StripeWebhookService stripeWebhookService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        stripeWebhookService.handleWebhook(payload, signature);
        return ResponseEntity.ok(ApiResponse.success("Webhook accepted.", null));
    }
}
