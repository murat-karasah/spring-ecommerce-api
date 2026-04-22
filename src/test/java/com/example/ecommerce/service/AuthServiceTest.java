package com.example.ecommerce.service;

import com.example.ecommerce.dto.request.LoginRequest;
import com.example.ecommerce.dto.request.RegisterRequest;
import com.example.ecommerce.dto.response.AuthResponse;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.RefreshToken;
import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.RoleName;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.UnauthorizedException;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.RefreshTokenRepository;
import com.example.ecommerce.repository.RoleRepository;
import com.example.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock CartRepository cartRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    private Role userRole;
    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800000L);

        userRole = new Role(RoleName.ROLE_USER);
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .firstName("Test")
                .lastName("User")
                .roles(Set.of(userRole))
                .build();
    }

    @Test
    void register_newEmail_returnsTokens() {
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "Password1!", "Test", "User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(cartRepository.save(any(Cart.class))).thenReturn(Cart.builder().user(user).build());
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.email()).isEqualTo("test@example.com");
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void register_duplicateEmail_throwsBadRequestException() {
        RegisterRequest request = new RegisterRequest(
                "existing@example.com", "Password1!", "Test", "User");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("test@example.com", "Password1!");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(refreshTokenRepository).deleteAllByUser(user);
    }

    @Test
    void refresh_validToken_returnsNewAccessToken() {
        RefreshToken storedToken = RefreshToken.builder()
                .token("valid-refresh")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh"))
                .thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        AuthResponse response = authService.refresh(
                new com.example.ecommerce.dto.request.RefreshTokenRequest("valid-refresh"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("valid-refresh");
    }

    @Test
    void refresh_revokedToken_throwsUnauthorizedException() {
        RefreshToken revokedToken = RefreshToken.builder()
                .token("revoked-token")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revoked-token"))
                .thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> authService.refresh(
                new com.example.ecommerce.dto.request.RefreshTokenRequest("revoked-token")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void refresh_expiredToken_throwsUnauthorizedException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refresh(
                new com.example.ecommerce.dto.request.RefreshTokenRequest("expired-token")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
        verify(refreshTokenRepository).delete(expiredToken);
    }
}
