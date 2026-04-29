package com.ecommerce.shop.repository;

import com.ecommerce.shop.entity.Order;
import com.ecommerce.shop.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.id = :orderId AND o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithItems(@Param("orderId") Long orderId,
                                               @Param("userId") Long userId);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status NOT IN :excludedStatuses")
    Optional<BigDecimal> sumTotalPriceExcludingStatuses(@Param("excludedStatuses") List<OrderStatus> excludedStatuses);
}
