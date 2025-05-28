package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserId(Long userId);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.variant WHERE c.id = :id")
    Optional<Cart> findByIdWithItems(Long id);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product LEFT JOIN FETCH i.variant WHERE c.user.id = :userId")
    Optional<Cart> findByUserIdWithItems(Long userId);
} 