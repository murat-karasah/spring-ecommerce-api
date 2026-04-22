package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.AddToCartRequest;
import com.example.ecommerce.dto.request.UpdateCartItemRequest;
import com.example.ecommerce.dto.response.CartResponse;
import com.example.ecommerce.helper.CurrentUserHelper;
import com.example.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final CurrentUserHelper currentUserHelper;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart(currentUserHelper.getCurrentUser()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart (increments if already present)")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addItem(currentUserHelper.getCurrentUser(), request));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update item quantity")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long itemId,
                                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(
                cartService.updateItem(currentUserHelper.getCurrentUser(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(currentUserHelper.getCurrentUser(), itemId));
    }

    @DeleteMapping
    @Operation(summary = "Clear all items from cart")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(currentUserHelper.getCurrentUser());
        return ResponseEntity.noContent().build();
    }
}
