package com.uc.ms_security.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record TwoFactorVerifyRequestDTO(
        @NotBlank String challengeToken,
        @NotBlank String code) {
}
