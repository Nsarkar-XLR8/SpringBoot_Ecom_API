package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.CartItemRequest;
import com.ecommerce.shop.dto.request.UpdateCartItemRequest;
import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.CartResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Authenticated cart management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping("/my-carts")
    @Operation(summary = "Get current user cart")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Cart fetched!",
                        cartService.getMyCart(user))
        );
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            Authentication authentication,
            @RequestBody @Valid CartItemRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Item added to cart!",
                        cartService.addItem(user, request))
        );
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestBody @Valid UpdateCartItemRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Cart item updated!",
                        cartService.updateItem(user, itemId, request))
        );
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            Authentication authentication,
            @PathVariable Long itemId) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Cart item removed!",
                        cartService.removeItem(user, itemId))
        );
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear entire cart")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared!",
                        cartService.clearCart(user))
        );
    }
}
