package com.guilhermesemog.unimove.controller;

import com.guilhermesemog.unimove.dto.auth.*;
import com.guilhermesemog.unimove.service.AuthService;

import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        LoginResponse loginResponse = authService.register(registerRequest);
        return getAuthResponseResponseEntity(loginResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return getAuthResponseResponseEntity(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(value = "refreshToken") String refreshToken) {
        LoginResponse loginResponse = authService.refresh(refreshToken);
        return getAuthResponseResponseEntity(loginResponse);
    }

    @NonNull
    private ResponseEntity<AuthResponse> getAuthResponseResponseEntity(LoginResponse loginResponse) {
        AuthResponse authResponse = new AuthResponse(loginResponse.accessToken());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }
}
