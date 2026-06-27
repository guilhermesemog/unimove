package com.guilhermesemog.unimove.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
