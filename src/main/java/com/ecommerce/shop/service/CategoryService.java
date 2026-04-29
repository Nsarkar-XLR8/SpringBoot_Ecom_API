package com.ecommerce.shop.service;

import com.ecommerce.shop.dto.request.CategoryRequest;
import com.ecommerce.shop.dto.response.CategoryResponse;
import com.ecommerce.shop.entity.Category;
import com.ecommerce.shop.exception.BusinessException;
import com.ecommerce.shop.exception.ResourceNotFoundException;
import com.ecommerce.shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Create Category
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Category already exists!");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category saved = categoryRepository.save(category);
        // Using optimized count fetch for response
        return getCategoryById(saved.getId());
    }

    // Get All Categories - Optimized to resolve N+1 Query
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllWithProductCount();
    }

    // Get Category By ID - Optimized
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findByIdWithProductCount(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id
                ));
    }

    // Update Category
    public CategoryResponse updateCategory(Long id,
                                           CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id
                ));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        categoryRepository.save(category);
        return getCategoryById(id);
    }

    // Delete Category
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id
                ));
        categoryRepository.delete(category);
    }
}
