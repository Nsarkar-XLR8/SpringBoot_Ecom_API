package com.ecommerce.shop.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private Long cartId;
    private Long userId;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private List<CartItemResponse> items;
}
