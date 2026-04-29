package com.ecommerce.shop.repository;

import com.ecommerce.shop.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Optional<Wishlist> findByUserId(Long userId);

    @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.products WHERE w.user.id = :userId")
    Optional<Wishlist> findByUserIdWithProducts(@Param("userId") Long userId);
}
