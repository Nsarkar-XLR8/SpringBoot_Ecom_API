package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.request.AddressRequest;
import com.ecommerce.shop.dto.request.ChangePasswordRequest;
import com.ecommerce.shop.dto.request.UserProfileRequest;
import com.ecommerce.shop.dto.response.AddressResponse;
import com.ecommerce.shop.dto.response.UserResponse;
import com.ecommerce.shop.entity.Address;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.AddressRepository;
import com.ecommerce.shop.repository.UserRepository;
import com.stripe.model.Customer;
import com.stripe.param.CustomerUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(User user) {
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateMyProfile(User user, UserProfileRequest request) {
        boolean emailChanged = !user.getEmail().equalsIgnoreCase(request.getEmail());

        if (emailChanged && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already taken!");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Sync with Stripe Customer if exists
        if (emailChanged && user.getStripeCustomerId() != null) {
            try {
                Customer stripeCustomer = Customer.retrieve(user.getStripeCustomerId());
                CustomerUpdateParams params = CustomerUpdateParams.builder()
                        .setEmail(user.getEmail())
                        .setName(user.getName())
                        .build();
                stripeCustomer.update(params);
            } catch (Exception e) {
                log.error("Failed to update Stripe Customer email for user {}: {}", user.getId(), e.getMessage());
                // We don't throw here to avoid rolling back the local update, 
                // but we could depending on business requirements.
            }
        }

        return mapToUserResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Incorrect current password!");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(User user) {
        return addressRepository.findAllByUserId(user.getId()).stream()
                .map(this::mapToAddressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse addAddress(User user, AddressRequest request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.resetDefaultAddressesForUser(user.getId());
        }

        Address address = Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .zipCode(request.getZipCode())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .user(user)
                .build();

        return mapToAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(User user, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found!"));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.resetDefaultAddressesForUser(user.getId());
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);

        return mapToAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(User user, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found!"));
        addressRepository.delete(address);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .isDefault(address.getIsDefault())
                .build();
    }
}
