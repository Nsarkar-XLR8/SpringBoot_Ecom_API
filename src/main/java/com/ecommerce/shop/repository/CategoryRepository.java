package com.ecommerce.shop.repository;

import com.ecommerce.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    @Query("SELECT new com.ecommerce.shop.dto.response.CategoryResponse(c.id, c.name, c.description, COUNT(p.id)) " +
           "FROM Category c LEFT JOIN c.products p GROUP BY c.id, c.name, c.description ORDER BY c.name")
    List<com.ecommerce.shop.dto.response.CategoryResponse> findAllWithProductCount();

    @Query("SELECT new com.ecommerce.shop.dto.response.CategoryResponse(c.id, c.name, c.description, COUNT(p.id)) " +
           "FROM Category c LEFT JOIN c.products p WHERE c.id = :id GROUP BY c.id, c.name, c.description")
    Optional<com.ecommerce.shop.dto.response.CategoryResponse> findByIdWithProductCount(Long id);
}
