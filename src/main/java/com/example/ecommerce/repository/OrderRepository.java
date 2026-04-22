package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByUser(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    java.util.Optional<Order> findByIdAndUser(Long id, User user);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT o FROM Order o")
    Page<Order> findAllWithItems(Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    java.util.Optional<Order> findWithItemsById(Long id);
}
