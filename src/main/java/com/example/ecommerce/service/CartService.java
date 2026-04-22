package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.AddToCartRequest;
import com.example.ecommerce.dto.request.UpdateCartItemRequest;
import com.example.ecommerce.dto.response.CartResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.mapper.CartMapper;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    public CartResponse getCart(User user) {
        return cartMapper.toResponse(getOrCreateCart(user));
    }

    @Transactional
    public CartResponse addItem(User user, AddToCartRequest request) {
        Cart cart = getOrCreateCart(user);
        Product product = getProductOrThrow(request.productId());

        Optional<CartItem> existing = cartItemRepository.findByCartAndProduct(cart, product);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.quantity();
            validateStock(product, newQty);
            item.setQuantity(newQty);
        } else {
            validateStock(product, request.quantity());
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return cartMapper.toResponse(reloadCart(user));
    }

    @Transactional
    public CartResponse updateItem(User user, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(user);
        CartItem item = getItemOrThrow(itemId);

        assertItemBelongsToCart(item, cart);
        validateStock(item.getProduct(), request.quantity());

        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return cartMapper.toResponse(reloadCart(user));
    }

    @Transactional
    public CartResponse removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCart(user);
        CartItem item = getItemOrThrow(itemId);

        assertItemBelongsToCart(item, cart);
        cart.getItems().remove(item);
        cartRepository.save(cart);

        return cartMapper.toResponse(reloadCart(user));
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Returns cart for user, creating an empty one if it doesn't exist yet.
     * Used by AuthService on registration and by CartService on every request.
     */
    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).build();
            log.info("Creating new cart for user: {}", user.getEmail());
            return cartRepository.save(newCart);
        });
    }

    // ---- private helpers ----

    private Cart reloadCart(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private Product getProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private CartItem getItemOrThrow(Long itemId) {
        return cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));
    }

    private void assertItemBelongsToCart(CartItem item, Cart cart) {
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to current user");
        }
    }

    private void validateStock(Product product, int requestedQty) {
        if (product.getStockQuantity() < requestedQty) {
            throw new BadRequestException(
                    "Insufficient stock for '" + product.getName() + "'" +
                    " (available: " + product.getStockQuantity() + ", requested: " + requestedQty + ")");
        }
    }
}
