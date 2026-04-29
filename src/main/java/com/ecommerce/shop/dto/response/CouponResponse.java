package com.ecommerce.shop.dto.response;

import com.ecommerce.shop.enums.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private LocalDateTime expiryDate;
    private Boolean isActive;
    private Integer usageLimit;
    private Integer usageCount;
}
