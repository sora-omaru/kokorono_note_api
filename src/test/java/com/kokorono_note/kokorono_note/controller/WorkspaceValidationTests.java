package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.service.WorkspaceService;
import com.kokorono_note.kokorono_note.service.WorkspaceInviteService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkspaceValidationTests {
    @Test
    void rejectsOverlongWorkspaceNameBeforeCallingService() throws Exception {
        var service = mock(WorkspaceService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new WorkspaceController(service,
                mock(WorkspaceInviteService.class))).setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver()).build();
        mvc.perform(post("/api/v1/workspaces").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + "a".repeat(101) + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
