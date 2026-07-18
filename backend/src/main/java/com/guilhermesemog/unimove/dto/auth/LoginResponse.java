package com.guilhermesemog.unimove.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
