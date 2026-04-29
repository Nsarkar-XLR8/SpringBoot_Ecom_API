package com.ecommerce.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CouponValidationRequest {
    @NotBlank(message = "Coupon code is required")
    private String code;
}
