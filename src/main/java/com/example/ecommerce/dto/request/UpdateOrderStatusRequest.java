package com.example.ecommerce.dto.request;

import com.example.ecommerce.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {}
