package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.EventCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.EventResponse;
import com.kokorono_note.kokorono_note.service.EventService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventControllerTests {
    private final EventService eventService = mock(EventService.class);
    private final UUID accountId = UUID.randomUUID();
    private final UUID workspaceId = UUID.randomUUID();
    private final String path = "/api/v1/workspaces/" + workspaceId + "/events";
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new EventController(eventService))
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
    void createsEventUsingAuthenticatedAccountAndWorkspace() throws Exception {
        EventCreateRequest request = new EventCreateRequest("旅行");
        when(eventService.createEvent(accountId, workspaceId, request))
                .thenReturn(new EventResponse(1L, "旅行"));

        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"旅行\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("旅行"));
        verify(eventService).createEvent(accountId, workspaceId, request);
    }

    @Test
    void rejectsInvalidNames() throws Exception {
        for (String body : new String[]{"{}", "{\"name\":null}", "{\"name\":\"\"}",
                "{\"name\":\"   \"}", "{\"name\":\"" + "a".repeat(101) + "\"}"}) {
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(eventService);
    }

    @Test
    void preservesWorkspaceNotFoundStatus() throws Exception {
        when(eventService.createEvent(accountId, workspaceId, new EventCreateRequest("旅行")))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"旅行\"}"))
                .andExpect(status().isNotFound());
    }
    @Test
    void listsEvents() throws Exception {
        when(eventService.getEvents(accountId, workspaceId))
                .thenReturn(List.of(new EventResponse(1L, "旅行")));
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("旅行"));
        verify(eventService).getEvents(accountId, workspaceId);
    }

    @Test
    void getsSingleEvent() throws Exception {
        when(eventService.getEventById(accountId, workspaceId, 1L))
                .thenReturn(new EventResponse(1L, "旅行"));
        mvc.perform(get(path + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("旅行"));
        verify(eventService).getEventById(accountId, workspaceId, 1L);
    }

    @Test
    void preservesGetNotFoundStatuses() throws Exception {
        when(eventService.getEvents(accountId, workspaceId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        when(eventService.getEventById(accountId, workspaceId, 1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(get(path)).andExpect(status().isNotFound());
        mvc.perform(get(path + "/1")).andExpect(status().isNotFound());
    }
}
