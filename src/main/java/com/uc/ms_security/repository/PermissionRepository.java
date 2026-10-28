package com.uc.ms_security.repository;

import com.uc.ms_security.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    boolean existsByUrlAndMethodAndModel(String url, String method, String model);

    boolean existsByUrlAndMethodAndModelAndIdNot(
            String url,
            String method,
            String model,
            Long id
    );

    Optional<Permission> findFirstByUrlAndMethodOrderByIdAsc(
            String url,
            String method
    );

    @Query("""
            SELECT DISTINCT p
            FROM Permission p
            JOIN p.rolePermissions rp
            JOIN rp.role r
            JOIN r.userRoles ur
            WHERE ur.user.id = :userId
              AND UPPER(p.method) = UPPER(:method)
              AND p.url = :url
            """)
    List<Permission> findByUserIdAndMethodAndUrl(
            @Param("userId") Long userId,
            @Param("method") String method,
            @Param("url") String url
    );
}
