package com.example.ecommerce.controller;

import com.example.ecommerce.dto.request.UpdateOrderStatusRequest;
import com.example.ecommerce.dto.response.OrderResponse;
import com.example.ecommerce.dto.response.PageResponse;
import com.example.ecommerce.helper.CurrentUserHelper;
import com.example.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final CurrentUserHelper currentUserHelper;

    @PostMapping
    @Operation(summary = "Place order from current cart")
    public ResponseEntity<OrderResponse> placeOrder() {
        return ResponseEntity.ok(orderService.placeOrder(currentUserHelper.getCurrentUser()));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's orders")
    public ResponseEntity<PageResponse<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(orderService.getMyOrders(currentUserHelper.getCurrentUser(), pageable));
    }

    @GetMapping("/my/{orderId}")
    @Operation(summary = "Get specific order belonging to current user")
    public ResponseEntity<OrderResponse> getMyOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getMyOrder(currentUserHelper.getCurrentUser(), orderId));
    }

    // ---- Admin endpoints ----

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: list all orders")
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: get any order by id")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: update order status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request.status()));
    }
}
