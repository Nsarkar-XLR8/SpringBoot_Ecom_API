package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.request.CartItemRequest;
import com.ecommerce.shop.dto.request.UpdateCartItemRequest;
import com.ecommerce.shop.dto.response.CartItemResponse;
import com.ecommerce.shop.dto.response.CartResponse;
import com.ecommerce.shop.entity.Cart;
import com.ecommerce.shop.entity.CartItem;
import com.ecommerce.shop.entity.Product;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.enums.ProductStatus;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.CartItemRepository;
import com.ecommerce.shop.repository.CartRepository;
import com.ecommerce.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartResponse getMyCart(User user) {
        Cart cart = getOrCreateCartWithItems(user);
        return mapToResponse(cart, user.getId());
    }

    public CartResponse addItem(User user, CartItemRequest request) {
        Cart cart = getOrCreateCart(user);

        Product product = getProductOrThrow(request.getProductId());
        validateProductForCart(product);
        validateQuantity(product, request.getQuantity());

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (item == null) {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(item);
        } else {
            int updatedQuantity = item.getQuantity() + request.getQuantity();
            validateQuantity(product, updatedQuantity);
            item.setQuantity(updatedQuantity);
            cartItemRepository.save(item);
        }

        return mapToResponse(getCartWithItems(user.getId()), user.getId());
    }

    public CartResponse updateItem(User user, Long itemId,
                                   UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found with id: " + itemId
                ));

        validateProductForCart(item.getProduct());
        validateQuantity(item.getProduct(), request.getQuantity());

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return mapToResponse(getCartWithItems(user.getId()), user.getId());
    }

    public CartResponse removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found with id: " + itemId
                ));

        cartItemRepository.delete(item);
        return mapToResponse(getCartWithItems(user.getId()), user.getId());
    }

    public CartResponse clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCartId(cart.getId());
        return mapToResponse(getCartWithItems(user.getId()), user.getId());
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));
    }

    private Cart getOrCreateCartWithItems(User user) {
        return cartRepository.findByUserIdWithItems(user.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));
    }

    private Cart getCartWithItems(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart not found for user!"
                ));
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId
                ));
    }

    private void validateProductForCart(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessException("Product is not available!");
        }
    }

    private void validateQuantity(Product product, int quantity) {
        if (quantity < 1) {
            throw new BusinessException("Quantity must be at least 1!");
        }
        if (quantity > product.getStock()) {
            throw new BusinessException(
                    "Only " + product.getStock() + " item(s) available in stock."
            );
        }
    }

    private CartResponse mapToResponse(Cart cart, Long userId) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalItems = 0;

        for (CartItem item : cart.getCartItems()) {
            BigDecimal lineTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .imageUrl(item.getProduct().getImageUrl())
                    .unitPrice(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .lineTotal(lineTotal)
                    .build());

            totalItems += item.getQuantity();
            totalAmount = totalAmount.add(lineTotal);
        }

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(userId)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .items(itemResponses)
                .build();
    }
}
