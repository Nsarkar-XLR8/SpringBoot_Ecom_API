package com.ecommerce.shop.controller;

import com.ecommerce.shop.dto.request.CategoryRequest;
import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.dto.response.CategoryResponse;
import com.ecommerce.shop.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    // GET all categories — Public
    @GetMapping
    @Operation(summary = "List categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>>
    getAllCategories() {
        return ResponseEntity.ok(
                ApiResponse.success("Categories fetched!",
                        categoryService.getAllCategories())
        );
    }

    // GET single category — Public
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>>
    getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Category fetched!",
                        categoryService.getCategoryById(id))
        );
    }

    // POST create — Admin only
    @PostMapping
    @Operation(summary = "Create category")
    public ResponseEntity<ApiResponse<CategoryResponse>>
    createCategory(@RequestBody @Valid CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created!",
                        categoryService.createCategory(request)));
    }

    // PUT update — Admin only
    @PutMapping("/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CategoryResponse>>
    updateCategory(@PathVariable Long id,
                   @RequestBody @Valid CategoryRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Category updated!",
                        categoryService.updateCategory(id, request))
        );
    }

    // DELETE — Admin only
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>>
    deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category deleted!", null)
        );
    }
}
