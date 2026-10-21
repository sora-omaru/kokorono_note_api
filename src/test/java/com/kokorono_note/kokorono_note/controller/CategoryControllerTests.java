package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.CategoryCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.CategoryResponse;
import com.kokorono_note.kokorono_note.service.CategoryService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerTests {
    private final CategoryService categoryService = mock(CategoryService.class);
    private final UUID accountId = UUID.randomUUID();
    private final UUID workspaceId = UUID.randomUUID();
    private final String path = "/api/v1/workspaces/" + workspaceId + "/events/10/categories";
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new CategoryController(categoryService))
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
    void createsCategoryUsingAuthenticatedAccountAndWorkspace() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("持ち物");
        when(categoryService.createCategory(accountId, workspaceId, 10L, request))
                .thenReturn(new CategoryResponse(1L, "持ち物"));

        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"持ち物\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("持ち物"));
        verify(categoryService).createCategory(accountId, workspaceId, 10L, request);
    }

    @Test
    void rejectsInvalidNames() throws Exception {
        for (String body : new String[]{"{}", "{\"name\":null}", "{\"name\":\"\"}",
                "{\"name\":\"   \"}", "{\"name\":\"" + "a".repeat(101) + "\"}"}) {
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(categoryService);
    }

    @Test
    void preservesWorkspaceNotFoundStatus() throws Exception {
        when(categoryService.createCategory(accountId, workspaceId, 10L, new CategoryCreateRequest("持ち物")))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Workspace not found"));

        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"持ち物\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsCategories() throws Exception {
        when(categoryService.getCategories(accountId, workspaceId, 10L))
                .thenReturn(List.of(new CategoryResponse(1L, "持ち物")));
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("持ち物"));
        verify(categoryService).getCategories(accountId, workspaceId, 10L);
    }

    @Test
    void getsSingleCategory() throws Exception {
        when(categoryService.getCategoryById(accountId, workspaceId, 10L, 1L))
                .thenReturn(new CategoryResponse(1L, "持ち物"));
        mvc.perform(get(path + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("持ち物"));
        verify(categoryService).getCategoryById(accountId, workspaceId, 10L, 1L);
    }

    @Test
    void preservesGetNotFoundStatuses() throws Exception {
        when(categoryService.getCategories(accountId, workspaceId, 10L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        when(categoryService.getCategoryById(accountId, workspaceId, 10L, 1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mvc.perform(get(path)).andExpect(status().isNotFound());
        mvc.perform(get(path + "/1")).andExpect(status().isNotFound());
    }

    @Test
    void deletesCategory() throws Exception {
        mvc.perform(delete(path + "/1"))
                .andExpect(status().isNoContent());
        verify(categoryService).deleteCategory(accountId, workspaceId, 10L, 1L);
    }
}
