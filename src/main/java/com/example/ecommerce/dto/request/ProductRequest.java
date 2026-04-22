package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(max = 255)
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Price must be >= 0")
        @Digits(integer = 10, fraction = 2, message = "Price must have at most 2 decimal places")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity must be >= 0")
        Integer stockQuantity,

        @Size(max = 512)
        String imageUrl,

        Long categoryId
) {}
