package com.uc.ms_security.repository;

import com.uc.ms_security.entity.RolePermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    @Query(
            value = "SELECT COUNT(*) FROM role_permissions "
                    + "WHERE role_id = :roleId AND permission_id = :permissionId",
            nativeQuery = true
    )
    Long countByRoleIdAndPermissionId(
            @Param("roleId") Long roleId,
            @Param("permissionId") Long permissionId
    );

    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO role_permissions (role_id, permission_id) "
                    + "VALUES (:roleId, :permissionId)",
            nativeQuery = true
    )
    void insertAssignment(
            @Param("roleId") Long roleId,
            @Param("permissionId") Long permissionId
    );

    @EntityGraph(attributePaths = {"role", "permission"})
    List<RolePermission> findAllByRoleId(Long roleId);

    @EntityGraph(attributePaths = {"role", "permission"})
    List<RolePermission> findAllByPermissionId(Long permissionId);

    @Override
    @EntityGraph(attributePaths = {"role", "permission"})
    List<RolePermission> findAll();

    @EntityGraph(attributePaths = {"role", "permission"})
    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    boolean existsByPermissionId(Long permissionId);

    boolean existsByRoleId(Long roleId);
}
