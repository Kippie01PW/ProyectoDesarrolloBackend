package com.uc.ms_security.controller;

import com.uc.ms_security.dto.CreateUserDTO;
import com.uc.ms_security.dto.UserResponseDTO;
import com.uc.ms_security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compatibilidad con el endpoint de registro usado por la primera versión de
 * este servicio. El flujo canónico también está disponible en {@code /api/users}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody CreateUserDTO dto) {
        return userService.register(dto);
    }
}
