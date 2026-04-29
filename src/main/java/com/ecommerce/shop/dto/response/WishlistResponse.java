package com.ecommerce.shop.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class WishlistResponse {
    private Long id;
    private Long userId;
    private List<ProductResponse> products;
}
