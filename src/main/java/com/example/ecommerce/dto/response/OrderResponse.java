package com.example.ecommerce.dto.response;

import com.example.ecommerce.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        List<OrderItemResponse> items,
        OrderStatus status,
        BigDecimal total,
        LocalDateTime createdAt
) {}
