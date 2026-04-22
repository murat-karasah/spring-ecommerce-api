package com.example.ecommerce.dto.response;

import com.example.ecommerce.entity.User;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long userId,
        String email,
        List<String> roles
) {
    public static AuthResponse of(String accessToken, String refreshToken, User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .toList();
        return new AuthResponse(accessToken, refreshToken, "Bearer",
                user.getId(), user.getEmail(), roleNames);
    }
}
