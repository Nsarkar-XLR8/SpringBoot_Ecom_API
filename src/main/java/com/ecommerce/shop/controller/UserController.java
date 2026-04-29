package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.AddressRequest;
import com.ecommerce.shop.dto.request.ChangePasswordRequest;
import com.ecommerce.shop.dto.request.UserProfileRequest;
import com.ecommerce.shop.dto.response.AddressResponse;
import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.UserResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Authenticated user profile and address management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Profile fetched!", userService.getMyProfile(user)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            Authentication authentication,
            @RequestBody @Valid UserProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Profile updated!", userService.updateMyProfile(user, request)));
    }

    @PatchMapping("/me/password")
    @Operation(summary = "Change user password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @RequestBody @Valid ChangePasswordRequest request) {
        User user = (User) authentication.getPrincipal();
        userService.changePassword(user, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully!", null));
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "Get user addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getMyAddresses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Addresses fetched!", userService.getMyAddresses(user)));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add new address")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            Authentication authentication,
            @RequestBody @Valid AddressRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Address added!", userService.addAddress(user, request)));
    }

    @PutMapping("/me/addresses/{id}")
    @Operation(summary = "Update address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid AddressRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Address updated!", userService.updateAddress(user, id, request)));
    }

    @DeleteMapping("/me/addresses/{id}")
    @Operation(summary = "Delete address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            Authentication authentication,
            @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        userService.deleteAddress(user, id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted!", null));
    }
}
