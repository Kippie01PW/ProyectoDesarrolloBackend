package com.uc.ms_security.controller;

import com.uc.ms_security.dto.auth.AuthResponseDTO;
import com.uc.ms_security.dto.auth.FirebaseLoginRequestDTO;
import com.uc.ms_security.dto.auth.LoginRequestDTO;
import com.uc.ms_security.dto.auth.PasswordRecoveryRequestDTO;
import com.uc.ms_security.dto.auth.PasswordResetRequestDTO;
import com.uc.ms_security.dto.auth.RegisterRequestDTO;
import com.uc.ms_security.dto.auth.TwoFactorSettingDTO;
import com.uc.ms_security.dto.auth.TwoFactorVerifyRequestDTO;
import com.uc.ms_security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/2fa/verify")
    public AuthResponseDTO verifyTwoFactor(
            @Valid @RequestBody TwoFactorVerifyRequestDTO dto) {
        return authService.verifyTwoFactor(dto);
    }

    @PostMapping("/oauth/firebase")
    public AuthResponseDTO firebaseLogin(
            @Valid @RequestBody FirebaseLoginRequestDTO dto) {
        return authService.firebaseLogin(dto);
    }

    @PostMapping("/password-recovery")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordRecovery(
            @Valid @RequestBody PasswordRecoveryRequestDTO dto) {
        authService.requestPasswordRecovery(dto);
    }

    @PostMapping("/password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetRequestDTO dto) {
        authService.resetPassword(dto);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            authService.logout(header.substring(7));
        }
    }

    @PutMapping("/2fa")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setTwoFactor(
            Authentication authentication,
            @RequestBody TwoFactorSettingDTO dto) {
        authService.setTwoFactor(Long.valueOf(authentication.getName()), dto.enabled());
    }
}
