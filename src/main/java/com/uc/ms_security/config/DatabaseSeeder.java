package com.uc.ms_security.config;

import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.entity.UserRole;
import com.uc.ms_security.repository.PermissionRepository;
import com.uc.ms_security.repository.RolePermissionRepository;
import com.uc.ms_security.repository.RoleRepository;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private static final PermissionSeed[] ADMIN_PERMISSIONS = {
            new PermissionSeed("GET", "/api/users", "User"),
            new PermissionSeed("POST", "/api/users", "User"),
            new PermissionSeed("GET", "/api/users/{id}", "User"),
            new PermissionSeed("PUT", "/api/users/{id}", "User"),
            new PermissionSeed("DELETE", "/api/users/{id}", "User"),
            new PermissionSeed("PUT", "/api/users/{id}/status", "User"),
            new PermissionSeed("GET", "/api/users/{id}/profile", "Profile"),
            new PermissionSeed("GET", "/api/users/{id}/detail-with-sessions", "User"),
            new PermissionSeed("GET", "/api/users/{id}/detail-with-roles", "User"),
            new PermissionSeed("POST", "/api/users/{id}/profile", "Profile"),
            new PermissionSeed("PUT", "/api/users/{id}/profile", "Profile"),
            new PermissionSeed("DELETE", "/api/users/{id}/profile", "Profile"),
            new PermissionSeed("GET", "/api/profile/me", "Profile"),
            new PermissionSeed("PUT", "/api/profile/me", "Profile"),
            new PermissionSeed("GET", "/api/roles", "Role"),
            new PermissionSeed("POST", "/api/roles", "Role"),
            new PermissionSeed("GET", "/api/roles/{id}", "Role"),
            new PermissionSeed("PUT", "/api/roles/{id}", "Role"),
            new PermissionSeed("DELETE", "/api/roles/{id}", "Role"),
            new PermissionSeed("GET", "/api/permissions", "Permission"),
            new PermissionSeed("POST", "/api/permissions", "Permission"),
            new PermissionSeed("GET", "/api/permissions/{id}", "Permission"),
            new PermissionSeed("PUT", "/api/permissions/{id}", "Permission"),
            new PermissionSeed("DELETE", "/api/permissions/{id}", "Permission"),
            new PermissionSeed("GET", "/api/roles/{id}/detail-with-permissions", "Role"),
            new PermissionSeed("GET", "/api/user-roles", "UserRole"),
            new PermissionSeed("POST", "/api/user-roles", "UserRole"),
            new PermissionSeed("GET", "/api/user-roles/users/{id}", "UserRole"),
            new PermissionSeed("GET", "/api/user-roles/roles/{id}", "UserRole"),
            new PermissionSeed("DELETE", "/api/user-roles/{id}/{id}", "UserRole"),
            new PermissionSeed("GET", "/api/role-permissions", "RolePermission"),
            new PermissionSeed("POST", "/api/role-permissions", "RolePermission"),
            new PermissionSeed("GET", "/api/role-permissions/roles/{id}", "RolePermission"),
            new PermissionSeed("GET", "/api/role-permissions/permissions/{id}", "RolePermission"),
            new PermissionSeed("DELETE", "/api/role-permissions/{id}/{id}", "RolePermission"),
            new PermissionSeed("GET", "/api/users/{id}/sessions", "Session"),
            new PermissionSeed("DELETE", "/api/users/{id}/sessions/{id}", "Session")
    };

    // Los 79 permisos HTTP de ms-gestion-deportiva (ACUERDOS.md secciones 9.1–9.17 y 10.4):
    // Persona A (28) + Persona B (27) + Persona C (24).
    public static final PermissionSeed[] SPORTS_ADMIN_PERMISSIONS = {
            // 9.1 Deportes (A - HU-GD-01)
            new PermissionSeed("POST", "/api/deportes", "Deporte"),
            new PermissionSeed("GET", "/api/deportes", "Deporte"),
            new PermissionSeed("GET", "/api/deportes/{id}", "Deporte"),
            new PermissionSeed("GET", "/api/deportes/{id}/subdeportes", "Deporte"),
            new PermissionSeed("PUT", "/api/deportes/{id}", "Deporte"),
            new PermissionSeed("PATCH", "/api/deportes/{id}/estado", "Deporte"),
            new PermissionSeed("DELETE", "/api/deportes/{id}", "Deporte"),
            // 9.2 Equipos (A - HU-GD-02)
            new PermissionSeed("POST", "/api/equipos", "Equipo"),
            new PermissionSeed("GET", "/api/equipos", "Equipo"),
            new PermissionSeed("GET", "/api/equipos/{id}", "Equipo"),
            new PermissionSeed("PUT", "/api/equipos/{id}", "Equipo"),
            new PermissionSeed("PATCH", "/api/equipos/{id}/estado", "Equipo"),
            new PermissionSeed("DELETE", "/api/equipos/{id}", "Equipo"),
            // 9.3 Equipo <-> Deporte (A - HU-GD-03)
            new PermissionSeed("POST", "/api/equipos/{id}/deportes", "Equipo"),
            new PermissionSeed("GET", "/api/equipos/{id}/deportes", "Equipo"),
            new PermissionSeed("GET", "/api/deportes/{id}/equipos", "Deporte"),
            new PermissionSeed("DELETE", "/api/equipos/{id}/deportes/{id}", "Equipo"),
            // 9.4 Escenarios (A - solo lectura)
            new PermissionSeed("GET", "/api/escenarios", "Escenario"),
            // 9.5 Torneos (A - HU-GC-01)
            new PermissionSeed("POST", "/api/torneos", "Torneo"),
            new PermissionSeed("GET", "/api/torneos", "Torneo"),
            new PermissionSeed("GET", "/api/torneos/{id}", "Torneo"),
            new PermissionSeed("PUT", "/api/torneos/{id}", "Torneo"),
            new PermissionSeed("PATCH", "/api/torneos/{id}/estado", "Torneo"),
            new PermissionSeed("DELETE", "/api/torneos/{id}", "Torneo"),
            // 9.6 Inscripciones (A - HU-GC-02)
            new PermissionSeed("POST", "/api/torneos/{id}/inscripciones", "Torneo"),
            new PermissionSeed("GET", "/api/torneos/{id}/inscripciones", "Torneo"),
            new PermissionSeed("GET", "/api/equipos/{id}/torneos", "Equipo"),
            new PermissionSeed("DELETE", "/api/torneos/{id}/inscripciones/{id}", "Torneo"),
            // 9.7 Jugadores (B - HU-GD-04)
            new PermissionSeed("POST", "/api/jugadores", "Jugador"),
            new PermissionSeed("GET", "/api/jugadores", "Jugador"),
            new PermissionSeed("GET", "/api/jugadores/{id}", "Jugador"),
            new PermissionSeed("PUT", "/api/jugadores/{id}", "Jugador"),
            new PermissionSeed("PATCH", "/api/jugadores/{id}/estado", "Jugador"),
            new PermissionSeed("DELETE", "/api/jugadores/{id}", "Jugador"),
            // 9.8 Equipo <-> Jugador / contratos (B - HU-GD-05)
            new PermissionSeed("POST", "/api/equipos/{id}/jugadores", "Equipo"),
            new PermissionSeed("GET", "/api/equipos/{id}/jugadores", "Equipo"),
            new PermissionSeed("GET", "/api/jugadores/{id}/equipos", "Jugador"),
            new PermissionSeed("PATCH", "/api/equipos/{id}/jugadores/{id}/retiro", "Equipo"),
            // 9.9 Habilidades (B - HU-GD-06)
            new PermissionSeed("POST", "/api/habilidades", "Habilidad"),
            new PermissionSeed("GET", "/api/habilidades", "Habilidad"),
            new PermissionSeed("GET", "/api/habilidades/{id}", "Habilidad"),
            new PermissionSeed("PUT", "/api/habilidades/{id}", "Habilidad"),
            new PermissionSeed("DELETE", "/api/habilidades/{id}", "Habilidad"),
            // 9.10 Partidos (B - HU-GC-06)
            new PermissionSeed("POST", "/api/fases/{id}/partidos", "Fase"),
            new PermissionSeed("GET", "/api/fases/{id}/partidos", "Fase"),
            new PermissionSeed("GET", "/api/partidos", "Partido"),
            new PermissionSeed("GET", "/api/partidos/{id}", "Partido"),
            new PermissionSeed("PUT", "/api/partidos/{id}", "Partido"),
            new PermissionSeed("PATCH", "/api/partidos/{id}/estado", "Partido"),
            new PermissionSeed("DELETE", "/api/partidos/{id}", "Partido"),
            // 9.11 Resultados y anotaciones (B - HU-GC-07)
            new PermissionSeed("PUT", "/api/partidos/{id}/resultado", "Partido"),
            new PermissionSeed("POST", "/api/partidos/{id}/anotaciones", "Partido"),
            new PermissionSeed("GET", "/api/partidos/{id}/anotaciones", "Partido"),
            new PermissionSeed("PUT", "/api/anotaciones/{id}", "Partido"),
            new PermissionSeed("DELETE", "/api/anotaciones/{id}", "Partido"),
            // 9.12 Habilidad <-> Jugador (C - HU-GD-07)
            new PermissionSeed("POST", "/api/jugadores/{id}/habilidades", "Jugador"),
            new PermissionSeed("GET", "/api/jugadores/{id}/habilidades", "Jugador"),
            new PermissionSeed("GET", "/api/habilidades/{id}/jugadores", "Habilidad"),
            new PermissionSeed("DELETE", "/api/jugadores/{id}/habilidades/{id}", "Jugador"),
            // 9.13 Perfil deportivo (C - HU-GD-08)
            new PermissionSeed("GET", "/api/jugadores/{id}/perfil", "Jugador"),
            // 9.14 Fases (C - HU-GC-03)
            new PermissionSeed("GET", "/api/torneos/{id}/fases", "Torneo"),
            new PermissionSeed("GET", "/api/fases/{id}", "Fase"),
            new PermissionSeed("PATCH", "/api/fases/{id}", "Fase"),
            new PermissionSeed("DELETE", "/api/fases/{id}", "Fase"),
            // 9.15 Grupos (C - HU-GC-04)
            new PermissionSeed("POST", "/api/torneos/{id}/grupos", "Torneo"),
            new PermissionSeed("GET", "/api/torneos/{id}/grupos", "Torneo"),
            new PermissionSeed("GET", "/api/grupos/{id}", "Fase"),
            new PermissionSeed("PUT", "/api/grupos/{id}", "Fase"),
            new PermissionSeed("DELETE", "/api/grupos/{id}", "Fase"),
            new PermissionSeed("POST", "/api/grupos/{id}/equipos", "Fase"),
            new PermissionSeed("GET", "/api/grupos/{id}/equipos", "Fase"),
            new PermissionSeed("DELETE", "/api/grupos/{id}/equipos/{id}", "Fase"),
            new PermissionSeed("GET", "/api/grupos/{id}/tabla-posiciones", "Fase"),
            // 9.16 Fase final (C - HU-GC-05)
            new PermissionSeed("POST", "/api/torneos/{id}/fase-final", "Torneo"),
            new PermissionSeed("GET", "/api/torneos/{id}/fase-final", "Torneo"),
            new PermissionSeed("PUT", "/api/fases-finales/{id}", "Fase"),
            new PermissionSeed("DELETE", "/api/fases-finales/{id}", "Fase"),
            new PermissionSeed("GET", "/api/fases-finales/{id}/llaves", "Fase"),
            // 9.17 Desarrollo del torneo (C - HU-GC-08)
            new PermissionSeed("GET", "/api/torneos/{id}/desarrollo", "Torneo")
    };

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Bean
    CommandLineRunner seedDatabase() {
        return arguments -> {
            fixLegacySchemaConstraints();

            Role adminRole = findOrCreateRole("ADMIN", "Administrador");
            Role consultantRole = findOrCreateRole(
                    "CONSULTOR",
                    "Consulta de usuarios"
            );

            Role initialRole = findOrCreateRole(
                    "USUARIO INICIAL",
                    "Rol inicial para usuarios registrados"
            );

            Role sportsAdminRole = findOrCreateRole(
                    "ADMINISTRADOR DEPORTIVO",
                    "Administrador deportivo de gestión deportiva y competencias"
            );

            User adminUser = findOrCreateUser();
            assignRole(adminUser, adminRole);
            assignRole(adminUser, sportsAdminRole);

            for (PermissionSeed seed : ADMIN_PERMISSIONS) {
                Permission permission = findOrCreatePermission(seed);
                assignPermission(adminRole, permission);
            }

            for (PermissionSeed seed : SPORTS_ADMIN_PERMISSIONS) {
                Permission permission = findOrCreatePermission(seed);
                assignPermission(sportsAdminRole, permission);
            }

            Permission listUsers = findOrCreatePermission(
                    new PermissionSeed("GET", "/api/users", "User")
            );
            Permission getUser = findOrCreatePermission(
                    new PermissionSeed("GET", "/api/users/{id}", "User")
            );
            assignPermission(consultantRole, listUsers);
            assignPermission(consultantRole, getUser);

            assignPermission(initialRole, findOrCreatePermission(
                    new PermissionSeed("GET", "/api/profile/me", "Profile")));
            assignPermission(initialRole, findOrCreatePermission(
                    new PermissionSeed("PUT", "/api/profile/me", "Profile")));
        };
    }

    private void fixLegacySchemaConstraints() {
        if (jdbcTemplate == null) return;
        try {
            jdbcTemplate.execute("ALTER TABLE sessions MODIFY COLUMN active bit DEFAULT 1");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN auth_provider varchar(30) DEFAULT 'LOCAL'");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN enabled bit DEFAULT 1");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN active bit DEFAULT 1");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE profiles MODIFY COLUMN photo_url LONGTEXT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE password_reset_tokens MODIFY COLUMN token varchar(100) NULL DEFAULT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE password_reset_tokens MODIFY COLUMN expiration datetime NULL DEFAULT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE password_reset_tokens MODIFY COLUMN used bit NULL DEFAULT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE password_reset_tokens DROP INDEX token");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE password_reset_tokens DROP COLUMN token");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE password_reset_tokens DROP COLUMN expiration");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE password_reset_tokens DROP COLUMN used");
        } catch (Exception ignored) {}
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    return roleRepository.save(role);
                });
    }

    private User findOrCreateUser() {
        return userRepository.findByEmail("admin@uc.edu.co")
                .orElseGet(() -> {
                    User user = new User();
                    user.setName("Administrador");
                    user.setEmail("admin@uc.edu.co");
                    user.setPassword(passwordEncoder.encode("Admin123!"));
                    return userRepository.save(user);
                });
    }

    private Permission findOrCreatePermission(PermissionSeed seed) {
        Permission permission = permissionRepository.findByUrlAndMethod(seed.url(), seed.method())
                .orElseGet(() -> {
                    Permission created = new Permission();
                    created.setUrl(seed.url());
                    created.setMethod(seed.method());
                    created.setModel(seed.model());
                    return permissionRepository.save(created);
                });
        if (permission.getModel() == null || !permission.getModel().equals(seed.model())) {
            permission.setModel(seed.model());
            permission = permissionRepository.save(permission);
        }
        return permission;
    }

    private void assignRole(User user, Role role) {
        if (!userRoleRepository.existsByUserIdAndRoleId(
                user.getId(),
                role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    private void assignPermission(Role role, Permission permission) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(
                role.getId(),
                permission.getId())) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
    }

    public record PermissionSeed(String method, String url, String model) {
    }
}
