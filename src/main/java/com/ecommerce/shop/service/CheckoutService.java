package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.request.CheckoutRequest;
import com.ecommerce.shop.dto.response.CheckoutResponse;
import com.ecommerce.shop.entity.Cart;
import com.ecommerce.shop.entity.CartItem;
import com.ecommerce.shop.entity.CheckoutSession;
import com.ecommerce.shop.entity.CheckoutSessionItem;
import com.ecommerce.shop.entity.Product;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.enums.PaymentStatus;
import com.ecommerce.shop.enums.ProductStatus;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.CartRepository;
import com.ecommerce.shop.repository.CheckoutSessionItemRepository;
import com.ecommerce.shop.repository.CheckoutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final CartRepository cartRepository;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutSessionItemRepository checkoutSessionItemRepository;
    private final CheckoutPaymentProcessor checkoutPaymentProcessor;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${app.stripe.checkout.default-success-url:http://localhost:3000/checkout/success}")
    private String defaultSuccessUrl;

    @Value("${app.stripe.checkout.default-cancel-url:http://localhost:3000/checkout/cancel}")
    private String defaultCancelUrl;

    @Transactional
    public CheckoutResponse createCheckout(User user, CheckoutRequest request) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new BusinessException("Stripe secret key is not configured!");
        }

        Cart cart = cartRepository.findByUserIdWithItems(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found!"));

        if (cart.getCartItems().isEmpty()) {
            throw new BusinessException("Cart is empty!");
        }

        validateCartItems(cart.getCartItems());
        BigDecimal totalPrice = calculateTotal(cart.getCartItems());

        List<CheckoutLine> lines = buildCheckoutLines(cart.getCartItems());

        String successUrl = (request != null && hasText(request.getSuccessUrl()))
                ? request.getSuccessUrl() : defaultSuccessUrl;
        String cancelUrl = (request != null && hasText(request.getCancelUrl()))
                ? request.getCancelUrl() : defaultCancelUrl;
        String idempotencyKey = resolveIdempotencyKey(user, request);

        CheckoutSession existing = checkoutSessionRepository
                .findByUserIdAndIdempotencyKey(user.getId(), idempotencyKey)
                .orElse(null);
        if (existing != null && !Boolean.TRUE.equals(existing.getProcessed())) {
            return CheckoutResponse.builder()
                    .checkoutSessionId(existing.getId())
                    .stripeSessionId(existing.getStripeSessionId())
                    .checkoutUrl(existing.getCheckoutUrl())
                    .paymentStatus(existing.getPaymentStatus().name())
                    .build();
        }

        Map<String, Object> stripeSession = createStripeCheckoutSession(
                lines, successUrl, cancelUrl, idempotencyKey
        );
        String sessionId = String.valueOf(stripeSession.get("id"));
        String checkoutUrl = String.valueOf(stripeSession.get("url"));
        String paymentIntent = stripeSession.get("payment_intent") == null
                ? null : String.valueOf(stripeSession.get("payment_intent"));
        LocalDateTime expiresAt = getExpiresAt(stripeSession.get("expires_at"));

        CheckoutSession checkoutSession = checkoutSessionRepository.save(
                CheckoutSession.builder()
                        .user(user)
                        .stripeSessionId(sessionId)
                        .stripePaymentIntent(paymentIntent)
                        .idempotencyKey(idempotencyKey)
                        .checkoutUrl(checkoutUrl)
                        .paymentStatus(PaymentStatus.PENDING)
                        .processed(false)
                        .totalPrice(totalPrice)
                        .currency("usd")
                        .expiresAt(expiresAt)
                        .build()
        );

        List<CheckoutSessionItem> snapshotItems = new ArrayList<>();
        for (CheckoutLine line : lines) {
            snapshotItems.add(CheckoutSessionItem.builder()
                    .checkoutSession(checkoutSession)
                    .product(line.product())
                    .quantity(line.quantity())
                    .unitPrice(line.unitPrice())
                    .build());
        }
        checkoutSessionItemRepository.saveAll(snapshotItems);

        return CheckoutResponse.builder()
                .checkoutSessionId(checkoutSession.getId())
                .stripeSessionId(sessionId)
                .checkoutUrl(checkoutUrl)
                .paymentStatus(checkoutSession.getPaymentStatus().name())
                .build();
    }

    public void validatePendingPayments() {
        List<Long> pendingIds =
                checkoutSessionRepository.findPendingIdsForValidation(PaymentStatus.PENDING);

        for (Long checkoutSessionId : pendingIds) {
            try {
                checkoutPaymentProcessor.processSession(checkoutSessionId);
            } catch (BusinessException ex) {
                log.warn("Checkout validation business failure for session id={}: {}",
                        checkoutSessionId, ex.getMessage());
            } catch (Exception ex) {
                log.error("Checkout payment validation failed for session id={}",
                        checkoutSessionId, ex);
            }
        }
    }

    private void validateCartItems(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BusinessException("Product is not available: " + product.getName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }
        }
    }

    private List<CheckoutLine> buildCheckoutLines(List<CartItem> cartItems) {
        List<CheckoutLine> lines = new ArrayList<>();
        for (CartItem item : cartItems) {
            lines.add(new CheckoutLine(
                    item.getProduct(),
                    item.getQuantity(),
                    item.getProduct().getPrice()
            ));
        }
        return lines;
    }

    private BigDecimal calculateTotal(List<CartItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    @Retryable(
            retryFor = {HttpStatusCodeException.class},
            maxAttemptsExpression = "${app.retry.stripe.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${app.retry.stripe.backoff-ms:500}")
    )
    private Map<String, Object> createStripeCheckoutSession(
            List<CheckoutLine> lines, String successUrl, String cancelUrl, String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(stripeSecretKey);
        headers.set("Idempotency-Key", idempotencyKey);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("mode", "payment");
        form.add("success_url", successUrl + "?session_id={CHECKOUT_SESSION_ID}");
        form.add("cancel_url", cancelUrl);

        int i = 0;
        for (CheckoutLine line : lines) {
            String prefix = "line_items[" + i + "]";
            form.add(prefix + "[price_data][currency]", "usd");
            form.add(prefix + "[price_data][product_data][name]", line.product().getName());
            form.add(prefix + "[price_data][unit_amount]",
                    String.valueOf(toStripeAmount(line.unitPrice())));
            form.add(prefix + "[quantity]", String.valueOf(line.quantity()));
            i++;
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.stripe.com/v1/checkout/sessions",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("Failed to create Stripe checkout session!");
            }
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            throw new BusinessException("Stripe checkout error: " + ex.getResponseBodyAsString());
        }
    }

    private LocalDateTime getExpiresAt(Object expiresAtRaw) {
        if (expiresAtRaw == null) {
            return null;
        }
        long epoch = Long.parseLong(String.valueOf(expiresAtRaw));
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }

    private long toStripeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValue();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveIdempotencyKey(User user, CheckoutRequest request) {
        if (request != null && hasText(request.getClientRequestId())) {
            return "checkout-" + user.getId() + "-" + request.getClientRequestId().trim();
        }
        return "checkout-" + user.getId() + "-" + UUID.randomUUID();
    }

    private record CheckoutLine(Product product, int quantity, BigDecimal unitPrice) {
    }
}
