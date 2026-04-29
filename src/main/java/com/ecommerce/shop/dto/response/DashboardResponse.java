package com.ecommerce.shop.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Long totalUsers;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private List<ProductResponse> popularProducts;
}
