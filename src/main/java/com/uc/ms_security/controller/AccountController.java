package com.uc.ms_security.controller;

import com.uc.ms_security.dto.account.ForgotPasswordRequestDTO;
import com.uc.ms_security.dto.account.ResetPasswordRequestDTO;
import com.uc.ms_security.dto.user.CreateUserDTO;
import com.uc.ms_security.dto.user.UserResponseDTO;
import com.uc.ms_security.service.PasswordResetService;
import com.uc.ms_security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody CreateUserDTO dto) {
        return userService.create(dto);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        passwordResetService.forgotPassword(dto);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {
        passwordResetService.resetPassword(dto);
    }
}
