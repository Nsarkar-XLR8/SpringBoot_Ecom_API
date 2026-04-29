package com.ecommerce.shop.service;

import com.ecommerce.shop.entity.CheckoutSession;
import com.ecommerce.shop.entity.CheckoutSessionItem;
import com.ecommerce.shop.entity.Order;
import com.ecommerce.shop.entity.OrderItem;
import com.ecommerce.shop.entity.Product;
import com.ecommerce.shop.enums.OrderStatus;
import com.ecommerce.shop.enums.PaymentStatus;
import com.ecommerce.shop.enums.ProductStatus;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.CartItemRepository;
import com.ecommerce.shop.repository.CartRepository;
import com.ecommerce.shop.repository.CheckoutSessionRepository;
import com.ecommerce.shop.repository.OrderItemRepository;
import com.ecommerce.shop.repository.OrderRepository;
import com.ecommerce.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutPaymentProcessor {

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.stripe.secret-key:}")
    private String stripeSecretKey;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSession(Long checkoutSessionId) {
        CheckoutSession session = checkoutSessionRepository
                .findByIdForProcessing(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Checkout session not found with id: " + checkoutSessionId
                ));

        if (Boolean.TRUE.equals(session.getProcessed())) {
            return;
        }

        Map<String, Object> stripe = fetchStripeSession(session.getStripeSessionId());
        String paymentStatus = String.valueOf(stripe.get("payment_status"));
        String sessionStatus = String.valueOf(stripe.get("status"));
        Object paymentIntent = stripe.get("payment_intent");
        if (paymentIntent != null) {
            session.setStripePaymentIntent(String.valueOf(paymentIntent));
        }

        if ("paid".equalsIgnoreCase(paymentStatus)) {
            finalizePaidCheckout(session);
            return;
        }

        if ("expired".equalsIgnoreCase(sessionStatus)) {
            session.setPaymentStatus(PaymentStatus.EXPIRED);
            session.setProcessed(true);
            checkoutSessionRepository.save(session);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processByStripeSessionId(String stripeSessionId) {
        Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findByStripeSessionId(stripeSessionId);
        if (sessionOpt.isEmpty()) {
            log.warn("Checkout session not found for stripe session id={}", stripeSessionId);
            return;
        }
        processSession(sessionOpt.get().getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markRefundedByPaymentIntent(String paymentIntentId) {
        checkoutSessionRepository.findByStripePaymentIntent(paymentIntentId)
                .ifPresent(session -> {
                    session.setPaymentStatus(PaymentStatus.REFUNDED);
                    if (session.getOrder() != null) {
                        session.getOrder().setStatus(OrderStatus.REFUNDED);
                    }
                    checkoutSessionRepository.save(session);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDisputedByPaymentIntent(String paymentIntentId) {
        checkoutSessionRepository.findByStripePaymentIntent(paymentIntentId)
                .ifPresent(session -> {
                    session.setPaymentStatus(PaymentStatus.DISPUTED);
                    if (session.getOrder() != null) {
                        session.getOrder().setStatus(OrderStatus.DISPUTED);
                    }
                    checkoutSessionRepository.save(session);
                });
    }

    private void finalizePaidCheckout(CheckoutSession session) {
        // Optimization: Fetch all products with pessimistic write lock only once
        Map<Long, Product> productMap = new HashMap<>();
        for (CheckoutSessionItem item : session.getItems()) {
            Product product = productRepository.findByIdForUpdate(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + item.getProduct().getId()
                    ));

            if (product.getStock() < item.getQuantity()) {
                session.setPaymentStatus(PaymentStatus.FAILED);
                session.setProcessed(true);
                checkoutSessionRepository.save(session);
                log.warn("Checkout session {} failed due to insufficient stock for product {}",
                        session.getId(), product.getId());
                return;
            }
            productMap.put(product.getId(), product);
        }

        Order order = orderRepository.save(Order.builder()
                .user(session.getUser())
                .status(OrderStatus.CONFIRMED)
                .totalPrice(session.getTotalPrice())
                .build());

        List<OrderItem> orderItems = new ArrayList<>();
        for (CheckoutSessionItem item : session.getItems()) {
            Product product = productMap.get(item.getProduct().getId());

            // Deduct stock
            product.setStock(product.getStock() - item.getQuantity());
            if (product.getStock() == 0) {
                product.setStatus(ProductStatus.OUT_OF_STOCK);
            }
            productRepository.save(product);

            orderItems.add(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(item.getQuantity())
                    .price(item.getUnitPrice())
                    .build());
        }
        orderItemRepository.saveAll(orderItems);

        // Clear the user's cart upon successful checkout
        cartRepository.findByUserId(session.getUser().getId())
                .ifPresent(cart -> cartItemRepository.deleteByCartId(cart.getId()));

        session.setOrder(order);
        session.setPaymentStatus(PaymentStatus.PAID);
        session.setProcessed(true);
        checkoutSessionRepository.save(session);
    }

    @Retryable(
            retryFor = {HttpStatusCodeException.class},
            maxAttemptsExpression = "${app.retry.stripe.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${app.retry.stripe.backoff-ms:500}")
    )
    private Map<String, Object> fetchStripeSession(String sessionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stripeSecretKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.stripe.com/v1/checkout/sessions/" + sessionId,
                    HttpMethod.GET,
                    request,
                    Map.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("Failed to fetch Stripe session!");
            }
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            String message = "Stripe validation error for session " + sessionId + ": "
                    + ex.getResponseBodyAsString();
            log.error(message);
            throw new BusinessException(message);
        }
    }
}
