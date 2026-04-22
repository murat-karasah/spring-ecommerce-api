package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.RefreshTokenRequest;
import com.example.ecommerce.dto.request.RegisterRequest;
import com.example.ecommerce.dto.response.AuthResponse;
import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.RoleName;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.exception.UnauthorizedException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.RefreshTokenRepository;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.entity.Cart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already in use: " + request.email());
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_USER not found — run V2 migration"));

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);

        // Create empty cart for the new user (1:1 rule from spec)
        cartRepository.save(Cart.builder().user(user).build());
        log.info("New user registered: {}", user.getEmail());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.of(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User", null));

        // Revoke all existing refresh tokens for this user on new login
        refreshTokenRepository.deleteAllByUser(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());
        return AuthResponse.of(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (storedToken.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new UnauthorizedException("Refresh token has expired — please log in again");
        }

        User user = storedToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.of(newAccessToken, storedToken.getToken(), user);
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }
}
