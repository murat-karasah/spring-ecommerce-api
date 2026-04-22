package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse placeOrder(User user) {
        Cart cart = cartService.getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place order: cart is empty");
        }

        // Validate stock and deduct for each item
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int requested = cartItem.getQuantity();
            if (product.getStockQuantity() < requested) {
                throw new BadRequestException(
                        "Insufficient stock for '" + product.getName() + "'" +
                        " (available: " + product.getStockQuantity() + ", requested: " + requested + ")");
            }
            product.setStockQuantity(product.getStockQuantity() - requested);
            productRepository.save(product);
        }

        // Build order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.ZERO)
                .build();

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .unitPrice(cartItem.getProduct().getPrice())
                        .build())
                .toList();

        order.getItems().addAll(orderItems);

        BigDecimal total = orderItems.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);

        Order saved = orderRepository.save(order);

        // Clear cart after successful order
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("Order #{} placed by user: {}", saved.getId(), user.getEmail());
        return orderMapper.toResponse(saved);
    }

    public PageResponse<OrderResponse> getMyOrders(User user, Pageable pageable) {
        Page<Order> page = orderRepository.findByUser(user, pageable);
        return PageResponse.of(page.map(orderMapper::toResponse));
    }

    public OrderResponse getMyOrder(User user, Long orderId) {
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return orderMapper.toResponse(order);
    }

    // Admin: get all orders
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> page = orderRepository.findAllWithItems(pageable);
        return PageResponse.of(page.map(orderMapper::toResponse));
    }

    // Admin: get any order by id
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.setStatus(newStatus);
        return orderMapper.toResponse(orderRepository.save(order));
    }
}
