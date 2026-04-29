package com.ecommerce.shop.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private Long checkoutSessionId;
    private String checkoutUrl;
    private String stripeSessionId;
    private String paymentStatus;
}
