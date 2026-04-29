package com.ecommerce.shop.service;

import com.ecommerce.shop.entity.WebhookEvent;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.repository.WebhookEventRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Dispute;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final WebhookEventRepository webhookEventRepository;
    private final CheckoutPaymentProcessor checkoutPaymentProcessor;

    @Value("${app.stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            throw new BusinessException("Stripe webhook secret is not configured.");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
        } catch (SignatureVerificationException ex) {
            throw new BusinessException("Invalid Stripe webhook signature.");
        }

        WebhookEvent webhookEvent = webhookEventRepository
                .findByEventId(event.getId())
                .orElse(null);
        if (webhookEvent != null && Boolean.TRUE.equals(webhookEvent.getProcessed())) {
            return;
        }

        if (webhookEvent == null) {
            webhookEvent = webhookEventRepository.save(
                    WebhookEvent.builder()
                            .eventId(event.getId())
                            .eventType(event.getType())
                            .processed(false)
                            .build()
            );
        }

        processEvent(event);

        webhookEvent.setProcessed(true);
        webhookEventRepository.save(webhookEvent);
    }

    private void processEvent(Event event) {
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        if (dataObjectDeserializer.getObject().isEmpty()) {
            log.warn("Stripe event {} has no deserialized object", event.getId());
            return;
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                Session session = (Session) dataObjectDeserializer.getObject().get();
                checkoutPaymentProcessor.processByStripeSessionId(session.getId());
            }
            case "charge.refunded" -> {
                Charge charge = (Charge) dataObjectDeserializer.getObject().get();
                if (charge.getPaymentIntent() != null) {
                    checkoutPaymentProcessor.markRefundedByPaymentIntent(charge.getPaymentIntent());
                }
            }
            case "charge.dispute.created" -> {
                Dispute dispute = (Dispute) dataObjectDeserializer.getObject().get();
                if (dispute.getPaymentIntent() != null) {
                    checkoutPaymentProcessor.markDisputedByPaymentIntent(dispute.getPaymentIntent());
                }
            }
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }
    }
}
