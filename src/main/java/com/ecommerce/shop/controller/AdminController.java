package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.CouponRequest;
import com.ecommerce.shop.dto.response.*;
import com.ecommerce.shop.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-specific management and reporting APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;

    // User Management
    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users fetched!", adminService.getAllUsers()));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Update user role (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        return ResponseEntity.ok(ApiResponse.success("User role updated!", adminService.updateUserRole(userId, role)));
    }

    // Dashboard & Reporting
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics (Admin only)")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched!", adminService.getDashboardStats()));
    }

    // Order Management for Admin
    @GetMapping("/orders")
    @Operation(summary = "Get all orders with pagination (Admin only)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(ApiResponse.success("Orders fetched!", adminService.getAllOrders(page, size, sortBy)));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get any order details by ID (Admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order details fetched!", adminService.getOrderById(id)));
    }

    // Coupon Management
    @PostMapping("/coupons")
    @Operation(summary = "Create new coupon (Admin only)")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @RequestBody @Valid CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created!", adminService.createCoupon(request)));
    }

    @GetMapping("/coupons")
    @Operation(summary = "Get all coupons (Admin only)")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        return ResponseEntity.ok(ApiResponse.success("Coupons fetched!", adminService.getAllCoupons()));
    }

    @DeleteMapping("/coupons/{id}")
    @Operation(summary = "Delete coupon (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
        adminService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("Coupon deleted!", null));
    }
}
