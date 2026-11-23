package com.uc.ms_security.controller;

import com.uc.ms_security.dto.PermissionValidationRequestDTO;
import com.uc.ms_security.dto.PermissionValidationResponseDTO;
import com.uc.ms_security.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/security")
@RequiredArgsConstructor
public class InternalSecurityController {

    private final PermissionService permissionService;

    @PostMapping("/permissions-validation")
    public PermissionValidationResponseDTO validatePermission(
            @Valid @RequestBody PermissionValidationRequestDTO dto,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return permissionService.validatePermission(userId, dto);
    }
}
