package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.response.WishlistResponse;
import com.ecommerce.shop.dto.response.ProductResponse;
import com.ecommerce.shop.entity.Product;
import com.ecommerce.shop.entity.User;
import com.ecommerce.shop.entity.Wishlist;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.ProductRepository;
import com.ecommerce.shop.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public WishlistResponse getMyWishlist(User user) {
        Wishlist wishlist = getOrCreateWishlist(user);
        return mapToWishlistResponse(wishlist);
    }

    public WishlistResponse addProductToWishlist(User user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!wishlist.getProducts().add(product)) {
            throw new BusinessException("Product already in wishlist.");
        }
        return mapToWishlistResponse(wishlistRepository.save(wishlist));
    }

    public WishlistResponse removeProductFromWishlist(User user, Long productId) {
        Wishlist wishlist = getOrCreateWishlist(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!wishlist.getProducts().remove(product)) {
            throw new BusinessException("Product not found in wishlist.");
        }
        return mapToWishlistResponse(wishlistRepository.save(wishlist));
    }

    public WishlistResponse clearWishlist(User user) {
        Wishlist wishlist = getOrCreateWishlist(user);
        wishlist.getProducts().clear();
        return mapToWishlistResponse(wishlistRepository.save(wishlist));
    }

    private Wishlist getOrCreateWishlist(User user) {
        return wishlistRepository.findByUserIdWithProducts(user.getId())
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder()
                        .user(user)
                        .build()));
    }

    private WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .products(wishlist.getProducts().stream()
                        .map(productService::mapToResponse)
                        .collect(Collectors.toList()))
                .build();
    }
}
