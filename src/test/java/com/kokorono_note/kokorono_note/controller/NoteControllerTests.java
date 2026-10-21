package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.NoteCreateRequest;
import com.kokorono_note.kokorono_note.dto.request.NoteUpdateRequest;
import com.kokorono_note.kokorono_note.dto.response.NoteResponse;
import com.kokorono_note.kokorono_note.service.NoteService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NoteControllerTests {
    private final NoteService noteService = mock(NoteService.class);
    private final UUID accountId = UUID.randomUUID();
    private final UUID workspaceId = UUID.randomUUID();
    private final Long eventId = 10L;
    private final Long categoryId = 20L;
    private final String path = "/api/v1/workspaces/" + workspaceId + "/events/" + eventId + "/categories/" + categoryId + "/notes";
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new NoteController(noteService))
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
    void createsNoteUsingAuthenticatedAccountAndWorkspace() throws Exception {
        NoteCreateRequest request = new NoteCreateRequest("旅行");
        when(noteService.createNote(accountId, workspaceId, eventId, categoryId, request))
                .thenReturn(new NoteResponse(1L, "旅行"));

        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"旅行\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("旅行"));
        verify(noteService).createNote(accountId, workspaceId, eventId, categoryId, request);
    }

    @Test
    void rejectsInvalidContent() throws Exception {
        for (String body : new String[]{"{}", "{\"content\":null}", "{\"content\":\"\"}",
                "{\"content\":\"   \"}", "{\"content\":\"" + "a".repeat(256) + "\"}"}) {
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(noteService);
    }

    @Test
    void preservesWorkspaceNotFoundStatus() throws Exception {
        when(noteService.createNote(accountId, workspaceId, eventId, categoryId, new NoteCreateRequest("旅行")))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"旅行\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsNotes() throws Exception {
        when(noteService.getNotes(accountId, workspaceId, eventId, categoryId))
                .thenReturn(List.of(new NoteResponse(1L, "旅行")));
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("旅行"));
        verify(noteService).getNotes(accountId, workspaceId, eventId, categoryId);
    }

    @Test
    void getsSingleNote() throws Exception {
        when(noteService.getNoteById(accountId, workspaceId, eventId, categoryId, 1L))
                .thenReturn(new NoteResponse(1L, "旅行"));
        mvc.perform(get(path + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("旅行"));
        verify(noteService).getNoteById(accountId, workspaceId, eventId, categoryId, 1L);
    }

    @Test
    void preservesGetNotFoundStatuses() throws Exception {
        when(noteService.getNotes(accountId, workspaceId, eventId, categoryId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        when(noteService.getNoteById(accountId, workspaceId, eventId, categoryId, 1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(get(path)).andExpect(status().isNotFound());
        mvc.perform(get(path + "/1")).andExpect(status().isNotFound());
    }

    @Test
    void updatesContentUsingPatch() throws Exception {
        when(noteService.updateNote(accountId, workspaceId, eventId, categoryId, 1L,
                new NoteUpdateRequest("更新本文"))).thenReturn(new NoteResponse(1L, "更新本文"));
        mvc.perform(patch(path + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"更新本文\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("更新本文"));
        verify(noteService).updateNote(accountId, workspaceId, eventId, categoryId, 1L,
                new NoteUpdateRequest("更新本文"));
    }

    @Test
    void rejectsInvalidPatchContent() throws Exception {
        for (String body : new String[]{"{}", "{\"content\":null}", "{\"content\":\"\"}",
                "{\"content\":\"   \"}", "{\"content\":\"" + "a".repeat(256) + "\"}"}) {
            mvc.perform(patch(path + "/1").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(noteService);
    }

    @Test
    void deletesNote() throws Exception {
        mvc.perform(delete(path + "/1")).andExpect(status().isNoContent());
        verify(noteService).deleteNote(accountId, workspaceId, eventId, categoryId, 1L);
    }

    @Test
    void preservesPatchNotFoundStatus() throws Exception {
        when(noteService.updateNote(accountId, workspaceId, eventId, categoryId, 1L,
                new NoteUpdateRequest("本文")))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(patch(path + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"本文\"}"))
                .andExpect(status().isNotFound());
    }

}
