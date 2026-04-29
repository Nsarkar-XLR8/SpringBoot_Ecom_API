package com.ecommerce.shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentValidationScheduler {

    private final CheckoutService checkoutService;

    @Scheduled(fixedDelayString = "${app.stripe.validation-interval-ms:5000}")
    public void validatePayments() {
        checkoutService.validatePendingPayments();
    }
}
