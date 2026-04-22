package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Eagerly load items and their products in one query
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findByUser(User user);
}
