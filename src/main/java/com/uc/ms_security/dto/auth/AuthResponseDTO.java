package com.uc.ms_security.dto.auth;

import java.time.Instant;

public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        boolean requiresTwoFactor,
        String challengeToken) {

    public AuthResponseDTO(String accessToken, String tokenType, Instant expiresAt) {
        this(accessToken, tokenType, expiresAt, false, null);
    }

    public static AuthResponseDTO challenge(String challengeToken, Instant expiresAt) {
        return new AuthResponseDTO(null, null, expiresAt, true, challengeToken);
    }
}
