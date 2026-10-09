package com.kokorono_note.kokorono_note.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
class LocalSecurityConfigTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsApiRequestsWithoutLoginOrCsrfToken() throws Exception {
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"invalid-local-code\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void doesNotRegisterGoogleLogin() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(post("/auth/token")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"invalid-local-code\"}"))
                .andExpect(status().isUnauthorized());
    }
}
