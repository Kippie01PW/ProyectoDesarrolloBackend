package com.uc.ms_security.controller;

import com.uc.ms_security.dto.account.ForgotPasswordRequestDTO;
import com.uc.ms_security.dto.account.ResetPasswordRequestDTO;
import com.uc.ms_security.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccountController {

    private final PasswordResetService passwordResetService;

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
