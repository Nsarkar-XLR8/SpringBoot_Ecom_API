package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.WishlistResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Authenticated user wishlist management APIs")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get current user wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> getMyWishlist(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Wishlist fetched!", wishlistService.getMyWishlist(user)));
    }

    @PostMapping("/items/{productId}")
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> addProductToWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Product added to wishlist!", wishlistService.addProductToWishlist(user, productId)));
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeProductFromWishlist(
            Authentication authentication,
            @PathVariable Long productId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist!", wishlistService.removeProductFromWishlist(user, productId)));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> clearWishlist(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Wishlist cleared!", wishlistService.clearWishlist(user)));
    }
}
