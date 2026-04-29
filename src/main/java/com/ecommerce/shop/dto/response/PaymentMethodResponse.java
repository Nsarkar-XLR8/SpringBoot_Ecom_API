package com.ecommerce.shop.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodResponse {
    private String id;
    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;
}
