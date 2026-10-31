package com.uc.ms_security.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordRecoveryRequestDTO(
        @NotBlank @Email String email) {
}
