package com.uc.ms_security.service;

import com.uc.ms_security.dto.permission.CreatePermissionDTO;
import com.uc.ms_security.dto.permission.PermissionResponseDTO;
import com.uc.ms_security.dto.permission.UpdatePermissionDTO;
import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.mapper.PermissionMapper;
import com.uc.ms_security.repository.PermissionRepository;
import com.uc.ms_security.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionResponseDTO create(CreatePermissionDTO dto) {
        String method = normalizeMethod(dto.getMethod());
        ensureUnique(dto.getUrl(), method, dto.getModel(), null);
        dto.setMethod(method);
        Permission permission = permissionMapper.toEntity(dto);
        return permissionMapper.toResponseDTO(permissionRepository.save(permission));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponseDTO> findAll() {
        return permissionMapper.toResponseDTOList(permissionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public PermissionResponseDTO findById(Long id) {
        return permissionMapper.toResponseDTO(findPermission(id));
    }

    public PermissionResponseDTO update(Long id, UpdatePermissionDTO dto) {
        Permission permission = findPermission(id);
        String method = normalizeMethod(dto.getMethod());
        ensureUnique(dto.getUrl(), method, dto.getModel(), id);
        dto.setMethod(method);
        permissionMapper.updateEntity(dto, permission);
        return permissionMapper.toResponseDTO(permissionRepository.save(permission));
    }

    public void delete(Long id) {
        Permission permission = findPermission(id);
        if (rolePermissionRepository.existsByPermissionId(id)) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "No se puede eliminar un permiso que está asignado"
            );
        }
        permissionRepository.delete(permission);
    }

    private void ensureUnique(String url, String method, String model, Long excludedId) {
        boolean exists = excludedId == null
                ? permissionRepository.existsByUrlAndMethodAndModel(url, method, model)
                : permissionRepository.existsByUrlAndMethodAndModelAndIdNot(
                        url,
                        method,
                        model,
                        excludedId
                );
        if (exists) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "Ya existe un permiso con esta URL, método y modelo"
            );
        }
    }

    private Permission findPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Permiso no encontrado con id: " + id
                ));
    }

    public boolean hasPermission(Long userId, String httpMethod, String requestUrl) {
        String normalizedUrl = normalizeUrl(requestUrl);
        return !permissionRepository.findByUserIdAndMethodAndUrl(
                userId,
                normalizeMethod(httpMethod),
                normalizedUrl
        ).isEmpty();
    }

    private String normalizeUrl(String requestUrl) {
        return requestUrl.replaceAll(
                "(?i)(?<=/)(?:\\d+|[a-f0-9]{24})(?=/|$)",
                "{id}"
        );
    }

    private String normalizeMethod(String method) {
        return method.toUpperCase(Locale.ROOT);
    }
}
