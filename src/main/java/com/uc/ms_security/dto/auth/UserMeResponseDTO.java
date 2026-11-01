package com.uc.ms_security.dto.auth;

import com.uc.ms_security.dto.RoleResponseDTO;
import com.uc.ms_security.dto.permission.PermissionResponseDTO;

import java.util.List;

public record UserMeResponseDTO(
        Long id,
        String name,
        String email,
        String authProvider,
        boolean twoFactorEnabled,
        List<RoleResponseDTO> roles,
        List<PermissionResponseDTO> permissions
) {}

