package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.ProductRequest;
import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.ProductResponse;
import com.ecommerce.shop.enums.ProductStatus;
import com.ecommerce.shop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management APIs")
public class ProductController {

    private final ProductService productService;

    // GET all products — Public
    @GetMapping("/all")
    @Operation(summary = "List products", description = "Returns paginated products with sorting.")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>>
    getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt")
            String sortBy) {
        return ResponseEntity.ok(
                ApiResponse.success("Products fetched!",
                        productService.getAllProducts(page, size, sortBy))
        );
    }

    // GET single product — Public
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>>
    getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched!",
                        productService.getProductById(id))
        );
    }

    // GET search products — Public
    @GetMapping("/search")
    @Operation(summary = "Advanced search and filter products", description = "Filter by name, category, price range, and status.")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>>
    searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success("Search results!",
                        productService.searchProducts(
                                name, categoryId, minPrice, maxPrice, status, page, size))
        );
    }

    // POST create — Admin only
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create product", description = "Creates a new product with image upload.")
    public ResponseEntity<ApiResponse<ProductResponse>>
    createProduct(
            @ModelAttribute @Valid ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created!",
                        productService.createProduct(request)));
    }

    // PUT update — Admin only
    @PutMapping(value = "/{id:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update product", description = "Updates product details and optionally the image.")
    public ResponseEntity<ApiResponse<ProductResponse>>
    updateProduct(@PathVariable Long id,
                  @ModelAttribute @Valid ProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated!",
                        productService.updateProduct(id, request))
        );
    }

    // DELETE — Admin only
    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "Delete product")
    public ResponseEntity<ApiResponse<Void>>
    deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted!", null)
        );
    }
}
