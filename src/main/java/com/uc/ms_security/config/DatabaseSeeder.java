package com.uc.ms_security.config;

import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.repository.PermissionRepository;
import com.uc.ms_security.repository.RolePermissionRepository;
import com.uc.ms_security.repository.RoleRepository;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private static final PermissionSeed[] ADMIN_PERMISSIONS = {
            new PermissionSeed("GET", "/api/users", "User"),
            new PermissionSeed("POST", "/api/users", "User"),
            new PermissionSeed("GET", "/api/users/{id}", "User"),
            new PermissionSeed("GET", "/api/users/{id}/detail", "User"),
            new PermissionSeed("GET", "/api/users/{id}/detail-with-sessions", "User"),
            new PermissionSeed("GET", "/api/users/{id}/detail-with-roles", "User"),
            new PermissionSeed("GET", "/api/users/available-for-role/{id}", "User"),
            new PermissionSeed("PUT", "/api/users/{id}", "User"),
            new PermissionSeed("DELETE", "/api/users/{id}", "User"),
            new PermissionSeed("GET", "/api/roles", "Role"),
            new PermissionSeed("POST", "/api/roles", "Role"),
            new PermissionSeed("GET", "/api/roles/{id}", "Role"),
            new PermissionSeed("GET", "/api/roles/{id}/detail-with-permissions", "Role"),
            new PermissionSeed("PUT", "/api/roles/{id}", "Role"),
            new PermissionSeed("DELETE", "/api/roles/{id}", "Role"),
            new PermissionSeed("GET", "/api/permissions", "Permission"),
            new PermissionSeed("POST", "/api/permissions", "Permission"),
            new PermissionSeed("GET", "/api/permissions/{id}", "Permission"),
            new PermissionSeed("PUT", "/api/permissions/{id}", "Permission"),
            new PermissionSeed("DELETE", "/api/permissions/{id}", "Permission"),
            new PermissionSeed("GET", "/api/user-roles", "UserRole"),
            new PermissionSeed("POST", "/api/user-roles", "UserRole"),
            new PermissionSeed("GET", "/api/user-roles/users/{id}", "UserRole"),
            new PermissionSeed("GET", "/api/user-roles/roles/{id}", "UserRole"),
            new PermissionSeed("GET", "/api/user-roles/roles/{id}/available-users", "UserRole"),
            new PermissionSeed("DELETE", "/api/user-roles/{id}/{id}", "UserRole"),
            new PermissionSeed("GET", "/api/role-permissions", "RolePermission"),
            new PermissionSeed("POST", "/api/role-permissions", "RolePermission"),
            new PermissionSeed("GET", "/api/role-permissions/roles/{id}", "RolePermission"),
            new PermissionSeed("GET", "/api/role-permissions/permissions/{id}", "RolePermission"),
            new PermissionSeed("DELETE", "/api/role-permissions/{id}/{id}", "RolePermission")
    };

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDatabase() {
        return arguments -> {
            Role adminRole = findOrCreateRole("ADMIN", "Administrador");
            Role consultantRole = findOrCreateRole(
                    "CONSULTOR",
                    "Consulta de usuarios"
            );

            User adminUser = findOrCreateUser();
            assignRole(adminUser, adminRole);

            for (PermissionSeed seed : ADMIN_PERMISSIONS) {
                assignPermission(adminRole, findOrCreatePermission(seed));
            }

            assignPermission(
                    consultantRole,
                    findOrCreatePermission(
                            new PermissionSeed("GET", "/api/users", "User")
                    )
            );
            assignPermission(
                    consultantRole,
                    findOrCreatePermission(
                            new PermissionSeed("GET", "/api/users/{id}", "User")
                    )
            );
        };
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            return roleRepository.save(role);
        });
    }

    private User findOrCreateUser() {
        return userRepository.findByEmail("admin@uc.edu.co").orElseGet(() -> {
            User user = new User();
            user.setName("Administrador");
            user.setEmail("admin@uc.edu.co");
            user.setPassword(passwordEncoder.encode("Admin123!"));
            return userRepository.save(user);
        });
    }

    private Permission findOrCreatePermission(PermissionSeed seed) {
        return permissionRepository
                .findFirstByUrlAndMethodOrderByIdAsc(seed.url(), seed.method())
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setUrl(seed.url());
                    permission.setMethod(seed.method());
                    permission.setModel(seed.model());
                    return permissionRepository.save(permission);
                });
    }

    private void assignRole(User user, Role role) {
        if (userRoleRepository.countByUserIdAndRoleId(user.getId(), role.getId()) == 0) {
            userRoleRepository.insertAssignment(user.getId(), role.getId());
        }
    }

    private void assignPermission(Role role, Permission permission) {
        if (rolePermissionRepository.countByRoleIdAndPermissionId(
                role.getId(), permission.getId()) == 0) {
            rolePermissionRepository.insertAssignment(
                    role.getId(), permission.getId());
        }
    }

    private record PermissionSeed(String method, String url, String model) {
    }
}
