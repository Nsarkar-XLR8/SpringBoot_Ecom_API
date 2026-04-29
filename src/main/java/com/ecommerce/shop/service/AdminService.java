package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.request.CouponRequest;
import com.ecommerce.shop.dto.response.*;
import com.ecommerce.shop.entity.Coupon;
import com.ecommerce.shop.entity.Order;
import com.ecommerce.shop.entity.OrderItem;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.enums.OrderStatus;
import com.ecommerce.shop.enums.Role;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.CouponRepository;
import com.ecommerce.shop.repository.OrderRepository;
import com.ecommerce.shop.repository.ProductRepository;
import com.ecommerce.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final ProductService productService;

    // User Management
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
        try {
            user.setRole(Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid role: " + role);
        }
        return mapToUserResponse(userRepository.save(user));
    }

    // Dashboard & Reporting
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();

        BigDecimal totalRevenue = orderRepository.sumTotalPriceExcludingStatuses(
                List.of(OrderStatus.CANCELLED, OrderStatus.REFUNDED)
        ).orElse(BigDecimal.ZERO);

        List<ProductResponse> popularProducts = productRepository.findAll(PageRequest.of(0, 5))
                .stream()
                .map(productService::mapToResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .popularProducts(popularProducts)
                .build();
    }

    // Order Management for Admin
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return orderRepository.findAll(pageable).map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToOrderResponse(order);
    }

    // Coupon Management
    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Coupon code already exists!");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .expiryDate(request.getExpiryDate())
                .usageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : 100)
                .build();

        return mapToCouponResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToCouponResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coupon not found!");
        }
        couponRepository.deleteById(id);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private CouponResponse mapToCouponResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .expiryDate(coupon.getExpiryDate())
                .isActive(coupon.getIsActive())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(this::mapOrderItem)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderItemResponse mapOrderItem(OrderItem item) {
        BigDecimal lineTotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .unitPrice(item.getPrice())
                .quantity(item.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }
}
