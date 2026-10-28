package com.uc.ms_security.repository;

import com.uc.ms_security.entity.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query(
            value = "SELECT COUNT(*) > 0 FROM user_roles "
                    + "WHERE user_id = :userId AND role_id = :roleId",
            nativeQuery = true
    )
    Long countByUserIdAndRoleId(
            @Param("userId") Long userId,
            @Param("roleId") Long roleId
    );

    @Modifying
    @Transactional
    @Query(
            value = "INSERT INTO user_roles (user_id, role_id) "
                    + "VALUES (:userId, :roleId)",
            nativeQuery = true
    )
    void insertAssignment(
            @Param("userId") Long userId,
            @Param("roleId") Long roleId
    );

    @EntityGraph(attributePaths = {"user", "role"})
    List<UserRole> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "role"})
    List<UserRole> findAllByRoleId(Long roleId);

    @Override
    @EntityGraph(attributePaths = {"user", "role"})
    List<UserRole> findAll();

    @EntityGraph(attributePaths = {"user", "role"})
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    boolean existsByRoleId(Long roleId);
}
