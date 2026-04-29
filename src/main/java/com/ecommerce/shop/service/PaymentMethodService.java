package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.response.PaymentMethodResponse;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodListParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final UserRepository userRepository;

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public List<PaymentMethodResponse> getMyPaymentMethods(User user) {
        String customerId = getOrCreateStripeCustomer(user);
        try {
            PaymentMethodListParams params = PaymentMethodListParams.builder()
                    .setCustomer(customerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build();

            PaymentMethodCollection paymentMethods = PaymentMethod.list(params);

            return paymentMethods.getData().stream()
                    .map(pm -> PaymentMethodResponse.builder()
                            .id(pm.getId())
                            .brand(pm.getCard().getBrand())
                            .last4(pm.getCard().getLast4())
                            .expMonth(pm.getCard().getExpMonth())
                            .expYear(pm.getCard().getExpYear())
                            .build())
                    .collect(Collectors.toList());
        } catch (StripeException e) {
            throw new BusinessException("Failed to fetch payment methods from Stripe: " + e.getMessage());
        }
    }

    public void deletePaymentMethod(User user, String paymentMethodId) {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            
            // Security check: ensure the payment method belongs to the user's stripe customer
            if (paymentMethod.getCustomer() == null || !paymentMethod.getCustomer().equals(user.getStripeCustomerId())) {
                throw new BusinessException("Payment method not found or doesn't belong to you.");
            }
            
            paymentMethod.detach();
        } catch (StripeException e) {
            throw new BusinessException("Failed to delete payment method from Stripe: " + e.getMessage());
        }
    }

    public String getOrCreateStripeCustomer(User user) {
        if (user.getStripeCustomerId() != null) {
            return user.getStripeCustomerId();
        }

        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getName())
                    .build();

            Customer customer = Customer.create(params);
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);
            return customer.getId();
        } catch (StripeException e) {
            throw new BusinessException("Failed to create Stripe customer: " + e.getMessage());
        }
    }
}
