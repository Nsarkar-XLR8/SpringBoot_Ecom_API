package com.ecommerce.shop.dto.request;

import lombok.Data;

@Data
public class CheckoutRequest {

    private String successUrl;

    private String cancelUrl;

    // Optional stable key from client/app to deduplicate checkout creation.
    private String clientRequestId;
}
