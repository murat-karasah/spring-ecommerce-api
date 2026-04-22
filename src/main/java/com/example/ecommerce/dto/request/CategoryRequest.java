package com.example.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(max = 100)
        String name,

        String description
) {}
