package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.response.CouponResponse;
import com.ecommerce.shop.entity.Cart;
import com.ecommerce.shop.entity.CartItem;
import com.ecommerce.shop.entity.Coupon;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.CartRepository;
import com.ecommerce.shop.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CartRepository cartRepository;

    @Transactional(readOnly = true)
    public CouponResponse validateCoupon(User user, String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));

        validateCouponEligibility(coupon);

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Your cart is empty."));

        BigDecimal cartTotal = calculateCartTotal(cart);

        if (cartTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BusinessException("Minimum order amount for this coupon is " + coupon.getMinOrderAmount());
        }

        return mapToCouponResponse(coupon);
    }

    private void validateCouponEligibility(Coupon coupon) {
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new BusinessException("This coupon is inactive.");
        }
        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("This coupon has expired.");
        }
        if (coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new BusinessException("This coupon has reached its usage limit.");
        }
    }

    private BigDecimal calculateCartTotal(Cart cart) {
        return cart.getCartItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
}
