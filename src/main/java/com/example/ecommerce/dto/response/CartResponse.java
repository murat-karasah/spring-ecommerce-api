package com.example.ecommerce.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        List<CartItemResponse> items,
        BigDecimal total
) {}
