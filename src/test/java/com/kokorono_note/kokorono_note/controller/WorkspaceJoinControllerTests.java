package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.response.WorkspaceResponse;
import com.kokorono_note.kokorono_note.service.WorkspaceInviteService;
import com.kokorono_note.kokorono_note.service.WorkspaceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkspaceJoinControllerTests {
    private final WorkspaceInviteService inviteService = mock(WorkspaceInviteService.class);
    private final UUID accountId = UUID.randomUUID();
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
                    new WorkspaceController(mock(WorkspaceService.class), inviteService))
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();

    @BeforeEach
    void authenticate() {
        Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "RS256")
                .subject(accountId.toString()).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void joinsUsingAuthenticatedAccountAndReturnsWorkspace() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        when(inviteService.joinWorkspace(accountId, "ABC123"))
                .thenReturn(new WorkspaceResponse(workspaceId, "Shared workspace"));

        mvc.perform(post("/api/v1/workspaces/join").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ABC123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(workspaceId.toString()))
                .andExpect(jsonPath("$.name").value("Shared workspace"));
        verify(inviteService).joinWorkspace(accountId, "ABC123");
    }

    @Test
    void rejectsMissingOrMalformedCode() throws Exception {
        for (String body : new String[]{"{}", "{\"code\":\"\"}", "{\"code\":\"abc123\"}",
                "{\"code\":\"ABC12\"}", "{\"code\":\"ABC1234\"}"}) {
            mvc.perform(post("/api/v1/workspaces/join").contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(inviteService);
    }

    @Test
    void preservesServiceErrorStatuses() throws Exception {
        for (HttpStatus status : new HttpStatus[]{HttpStatus.BAD_REQUEST, HttpStatus.CONFLICT}) {
            doThrow(new ResponseStatusException(status))
                    .when(inviteService).joinWorkspace(accountId, "ABC123");
            mvc.perform(post("/api/v1/workspaces/join").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"ABC123\"}"))
                    .andExpect(status().is(status.value()));
        }
    }
}
