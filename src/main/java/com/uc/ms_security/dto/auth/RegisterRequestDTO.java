package com.uc.ms_security.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email no tiene un formato válido")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])\\S{8,72}$",
                message = "La contraseña debe contener mayúscula, minúscula, número y carácter especial")
        String password) {
}
