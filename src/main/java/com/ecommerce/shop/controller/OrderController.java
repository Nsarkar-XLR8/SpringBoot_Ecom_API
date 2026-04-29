package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.OrderResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Authenticated order APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List current user orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Orders fetched!", orderService.getMyOrders(user))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get current user order by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getMyOrderById(
            Authentication authentication,
            @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Order fetched!", orderService.getMyOrderById(user, id))
        );
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel current user order")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelMyOrder(
            Authentication authentication,
            @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled!", orderService.cancelMyOrder(user, id))
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Admin: update order status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatusAsAdmin(
            @PathVariable Long id,
            @RequestBody @Valid UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order status updated!",
                        orderService.updateOrderStatusAsAdmin(id, request.getStatus())
                )
        );
    }
}
