package com.ecommerce.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private long totalProducts;

    // Additional constructor for JPA Projection that takes 'long' for the count
    public CategoryResponse(Long id, String name, String description, Long totalProducts) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalProducts = totalProducts != null ? totalProducts : 0L;
    }
}
