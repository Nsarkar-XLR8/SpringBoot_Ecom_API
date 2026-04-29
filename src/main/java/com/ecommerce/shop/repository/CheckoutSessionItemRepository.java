package com.ecommerce.shop.repository;

import com.ecommerce.shop.entity.CheckoutSessionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutSessionItemRepository extends JpaRepository<CheckoutSessionItem, Long> {
}
