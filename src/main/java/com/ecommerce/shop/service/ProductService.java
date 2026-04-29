package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.request.ProductRequest;
import com.ecommerce.shop.dto.response.ProductResponse;
import com.ecommerce.shop.entity.Category;
import com.ecommerce.shop.entity.Product;
import com.ecommerce.shop.enums.ProductStatus;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.CategoryRepository;
import com.ecommerce.shop.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageUploadService imageUploadService;

    // Create Product
    public ProductResponse createProduct(ProductRequest request) {

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found!"
                ));

        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = imageUploadService.uploadImage(request.getImage());
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(imageUrl)
                .category(category)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    // Get All Products with Pagination
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size,
                                                String sortBy) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(sortBy).descending());
        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // Get Product By ID
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id
                ));
        return mapToResponse(product);
    }

    // Advanced Search & Filtering
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name,
                                                Long categoryId,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                ProductStatus status,
                                                int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    // Update Product
    public ProductResponse updateProduct(Long id,
                                         ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id
                ));

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found!"
                ));

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            // Delete old image if exists
            if (product.getImageUrl() != null) {
                imageUploadService.deleteImage(product.getImageUrl());
            }
            // Upload new image
            product.setImageUrl(imageUploadService.uploadImage(request.getImage()));
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    // Delete Product
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id
                ));
        
        // Delete image from Cloudinary
        if (product.getImageUrl() != null) {
            imageUploadService.deleteImage(product.getImageUrl());
        }
        
        productRepository.delete(product);
    }

    // Update Stock
    public void updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found!"
                ));

        if (product.getStock() + quantity < 0) {
            throw new RuntimeException("Insufficient stock!");
        }

        product.setStock(product.getStock() + quantity);

        // Auto update status based on stock
        if (product.getStock() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }

        productRepository.save(product);
    }

    // Map Entity to Response
    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus().name())
                .categoryName(product.getCategory() != null
                        ? product.getCategory().getName()
                        : null)
                .build();
    }
}
