package com.example.ecommerce.service;

import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.entity.*;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock CartService cartService;
    @Mock OrderMapper orderMapper;

    @InjectMocks OrderService orderService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("user@example.com").build();

        product = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(BigDecimal.valueOf(1999.99))
                .stockQuantity(10)
                .build();

        cart = Cart.builder().id(1L).user(user).build();
        cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();
        cart.getItems().add(cartItem);
    }

    @Test
    void placeOrder_validCart_createsOrderAndDecrementsStock() {
        Order savedOrder = Order.builder()
                .id(1L)
                .user(user)
                .status(OrderStatus.PENDING)
                .total(BigDecimal.valueOf(3999.98))
                .items(new ArrayList<>())
                .build();

        OrderResponse expectedResponse = new OrderResponse(1L, 1L, List.of(),
                OrderStatus.PENDING, BigDecimal.valueOf(3999.98), null);

        when(cartService.getOrCreateCart(user)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

        OrderResponse result = orderService.placeOrder(user);

        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.total()).isEqualByComparingTo(BigDecimal.valueOf(3999.98));
        // Stock should have been decremented (10 - 2 = 8)
        assertThat(product.getStockQuantity()).isEqualTo(8);
        verify(productRepository).save(product);
        verify(cartRepository).save(cart);
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void placeOrder_emptyCart_throwsBadRequestException() {
        Cart emptyCart = Cart.builder().id(2L).user(user).build();
        when(cartService.getOrCreateCart(user)).thenReturn(emptyCart);

        assertThatThrownBy(() -> orderService.placeOrder(user))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cart is empty");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_insufficientStock_throwsBadRequestException() {
        product.setStockQuantity(1); // only 1 in stock, cart has qty 2
        when(cartService.getOrCreateCart(user)).thenReturn(cart);

        assertThatThrownBy(() -> orderService.placeOrder(user))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_existingOrder_updatesAndReturns() {
        Order order = Order.builder().id(1L).user(user).status(OrderStatus.PENDING)
                .total(BigDecimal.ZERO).items(new ArrayList<>()).build();
        OrderResponse response = new OrderResponse(1L, 1L, List.of(),
                OrderStatus.PAID, BigDecimal.ZERO, null);

        when(orderRepository.findWithItemsById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.updateStatus(1L, OrderStatus.PAID);

        assertThat(result.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void updateStatus_notFound_throwsResourceNotFoundException() {
        when(orderRepository.findWithItemsById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(99L, OrderStatus.PAID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMyOrder_notOwnedByUser_throwsResourceNotFoundException() {
        when(orderRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getMyOrder(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
