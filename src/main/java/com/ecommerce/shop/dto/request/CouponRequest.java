package com.ecommerce.shop.dto.request;

import com.ecommerce.shop.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    @NotBlank(message = "Coupon code is required")
    private String code;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    @NotNull(message = "Minimum order amount is required")
    private BigDecimal minOrderAmount;

    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiryDate;

    private Integer usageLimit;
}
