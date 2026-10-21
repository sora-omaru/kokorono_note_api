package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.response.TokenResponse;
import com.kokorono_note.kokorono_note.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTests {

    private final AuthService authService = mock(AuthService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();

    @Test
    void exchangesCodeAndSetsOneDayRefreshCookie() throws Exception {
        when(authService.exchangeCodeForAccessToken("auth-code"))
                .thenReturn(new TokenResponse("access", "refresh"));

        mvc.perform(post("/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"auth-code\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"accessToken\":\"access\"}"))
                .andExpect(cookie().value("refreshToken", "refresh"))
                .andExpect(cookie().maxAge("refreshToken", 86400));
    }

    @Test
    void refreshesAccessTokenAndReplacesCookie() throws Exception {
        when(authService.refresh("old-token")).thenReturn(new TokenResponse("new-access", "new-refresh"));

        mvc.perform(post("/auth/refresh").cookie(new Cookie("refreshToken", "old-token")))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"accessToken\":\"new-access\"}"))
                .andExpect(cookie().value("refreshToken", "new-refresh"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/auth"))
                .andExpect(cookie().maxAge("refreshToken", 86400))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")));
    }

    @Test
    void rejectsMissingOrBlankCookie() throws Exception {
        mvc.perform(post("/auth/refresh")).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/refresh").cookie(new Cookie("refreshToken", "")))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    void rejectsInvalidRefreshToken() throws Exception {
        when(authService.refresh("invalid")).thenThrow(new IllegalArgumentException("Invalid refresh Token"));

        mvc.perform(post("/auth/refresh").cookie(new Cookie("refreshToken", "invalid")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }
}
