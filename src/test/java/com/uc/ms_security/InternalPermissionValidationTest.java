package com.uc.ms_security;

import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Pruebas de integración del endpoint interno POST /api/internal/security/permissions-validation. */
class InternalPermissionValidationTest extends AbstractAuthTest {

    private static final String ENDPOINT = "/api/internal/security/permissions-validation";

    @Test
    void returnsAllowedTrueWhenUserHasMatchingPermissionWithNormalizedIds() throws Exception {
        User admin = localUser("deportivo@liga.test", true, false);
        Role sportsAdmin = role("ADMINISTRADOR DEPORTIVO");
        grant(sportsAdmin, "POST", "/api/jugadores/{id}/habilidades");
        grant(sportsAdmin, "DELETE", "/api/jugadores/{id}/habilidades/{id}");
        grant(sportsAdmin, "GET", "/api/fases-finales/{id}/llaves");
        grant(sportsAdmin, "GET", "/api/torneos/{id}/desarrollo");
        assign(admin, sportsAdmin);

        String token = loginToken("deportivo@liga.test");

        MvcResult r1 = validate(token, "/api/jugadores/7/habilidades", "POST");
        assertThat(r1.getResponse().getStatus()).isEqualTo(200);
        assertThat(field(r1, "$.allowed")).isEqualTo("true");

        MvcResult r2 = validate(token, "/api/jugadores/7/habilidades/2", "DELETE");
        assertThat(r2.getResponse().getStatus()).isEqualTo(200);
        assertThat(field(r2, "$.allowed")).isEqualTo("true");

        MvcResult r3 = validate(token, "/api/fases-finales/8/llaves", "GET");
        assertThat(r3.getResponse().getStatus()).isEqualTo(200);
        assertThat(field(r3, "$.allowed")).isEqualTo("true");

        // Normalización de método en minúsculas y slash final
        MvcResult r4 = validate(token, "/api/torneos/1/desarrollo/", "get");
        assertThat(r4.getResponse().getStatus()).isEqualTo(200);
        assertThat(field(r4, "$.allowed")).isEqualTo("true");
    }

    @Test
    void returnsAllowedFalseWhenUserLacksPermissionOrMethodDiffers() throws Exception {
        User user = localUser("sinpermisos@liga.test", true, false);
        Role limited = role("LIMITADO");
        grant(limited, "GET", "/api/torneos/{id}/fases");
        assign(user, limited);

        String token = loginToken("sinpermisos@liga.test");

        MvcResult wrongMethod = validate(token, "/api/torneos/1/fases", "DELETE");
        assertThat(wrongMethod.getResponse().getStatus()).isEqualTo(200);
        assertThat(field(wrongMethod, "$.allowed")).isEqualTo("false");

        MvcResult missingRoute = validate(token, "/api/torneos/1/desarrollo", "GET");
        assertThat(missingRoute.getResponse().getStatus()).isEqualTo(200);
        assertThat(field(missingRoute, "$.allowed")).isEqualTo("false");
    }

    @Test
    void returnsUnauthorizedWithoutValidActiveSession() throws Exception {
        User user = localUser("revocado@liga.test", true, false);
        Role sportsAdmin = role("ADMINISTRADOR DEPORTIVO");
        grant(sportsAdmin, "GET", "/api/torneos/{id}/fases");
        assign(user, sportsAdmin);

        String token = loginToken("revocado@liga.test");

        // Sin token
        MvcResult noToken = mockMvc.perform(post(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"/api/torneos/1/fases\",\"method\":\"GET\"}")).andReturn();
        assertThat(noToken.getResponse().getStatus()).isEqualTo(401);

        // Token mal formado
        MvcResult badToken = validate("no.es.jwt", "/api/torneos/1/fases", "GET");
        assertThat(badToken.getResponse().getStatus()).isEqualTo(401);

        // Tras logout, la sesión revocada devuelve 401
        assertThat(postWith("/api/auth/logout", token).getResponse().getStatus()).isEqualTo(204);
        MvcResult afterLogout = validate(token, "/api/torneos/1/fases", "GET");
        assertThat(afterLogout.getResponse().getStatus()).isEqualTo(401);

        // Usuario desactivado devuelve 401
        String token2 = loginToken("revocado@liga.test");
        user.setActive(false);
        userRepository.save(user);
        MvcResult disabled = validate(token2, "/api/torneos/1/fases", "GET");
        assertThat(disabled.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    void returnsBadRequestWhenUrlOrMethodIsBlank() throws Exception {
        localUser("validador@liga.test", true, false);
        String token = loginToken("validador@liga.test");

        MvcResult result = validate(token, " ", "");
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(field(result, "$.url")).isNotBlank();
        assertThat(field(result, "$.method")).isNotBlank();
    }

    private MvcResult validate(String token, String url, String method) throws Exception {
        return mockMvc.perform(post(ENDPOINT)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"%s\",\"method\":\"%s\"}".formatted(url, method)))
                .andReturn();
    }
}
