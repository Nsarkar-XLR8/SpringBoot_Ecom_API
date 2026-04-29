package com.ecommerce.shop.service;

import com.ecommerce.shop.entity.Order;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.enums.OrderStatus;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        orderService = new OrderService(orderRepository);
    }

    @Test
    void shouldRejectInvalidAdminStatusTransition() {
        Order order = Order.builder()
                .id(10L)
                .status(OrderStatus.DELIVERED)
                .totalPrice(BigDecimal.TEN)
                .build();
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.updateOrderStatusAsAdmin(10L, "SHIPPED"));
    }

    @Test
    void shouldAllowAdminValidStatusTransition() {
        Order order = Order.builder()
                .id(11L)
                .status(OrderStatus.CONFIRMED)
                .totalPrice(BigDecimal.TEN)
                .build();
        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = orderService.updateOrderStatusAsAdmin(11L, "SHIPPED");
        assertEquals("SHIPPED", response.getStatus());
    }

    @Test
    void userCanCancelPendingOrder() {
        User user = User.builder().id(5L).email("u@test.com").build();
        Order order = Order.builder()
                .id(12L)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ONE)
                .build();
        when(orderRepository.findByIdAndUserIdWithItems(12L, 5L))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = orderService.cancelMyOrder(user, 12L);
        assertEquals("CANCELLED", response.getStatus());
    }
}
