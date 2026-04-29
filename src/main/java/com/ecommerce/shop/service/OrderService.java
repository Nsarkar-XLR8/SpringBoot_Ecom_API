package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.response.OrderItemResponse;
import com.ecommerce.shop.dto.response.OrderResponse;
import com.ecommerce.shop.entity.Order;
import com.ecommerce.shop.entity.OrderItem;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.enums.OrderStatus;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public List<OrderResponse> getMyOrders(User user) {
        return orderRepository.findAllByUserIdWithItems(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getMyOrderById(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelMyOrder(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        if (!(order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED)) {
            throw new BusinessException("Order cannot be cancelled in current state: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateOrderStatusAsAdmin(Long orderId, String statusValue) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId
                ));

        OrderStatus target;
        try {
            target = OrderStatus.valueOf(statusValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid order status: " + statusValue);
        }

        validateTransition(order.getStatus(), target);
        order.setStatus(target);
        return mapToResponse(orderRepository.save(order));
    }

    private void validateTransition(OrderStatus current, OrderStatus target) {
        Map<OrderStatus, EnumSet<OrderStatus>> allowed = Map.of(
                OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
                OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED, OrderStatus.REFUNDED),
                OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.DISPUTED),
                OrderStatus.DELIVERED, EnumSet.of(OrderStatus.REFUNDED, OrderStatus.DISPUTED),
                OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class),
                OrderStatus.REFUNDED, EnumSet.noneOf(OrderStatus.class),
                OrderStatus.DISPUTED, EnumSet.of(OrderStatus.REFUNDED)
        );

        if (current == target) {
            return;
        }
        if (!allowed.getOrDefault(current, EnumSet.noneOf(OrderStatus.class)).contains(target)) {
            throw new BusinessException(
                    "Invalid order status transition: " + current + " -> " + target
            );
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(this::mapItem)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }

    private OrderItemResponse mapItem(OrderItem item) {
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
