package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.NoteCreateRequest;
import com.kokorono_note.kokorono_note.dto.request.NoteUpdateRequest;
import com.kokorono_note.kokorono_note.dto.response.NoteResponse;
import com.kokorono_note.kokorono_note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/events/{eventId}/categories/{categoryId}/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // 一覧取得
    @GetMapping
    public List<NoteResponse> getNotes(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId,
            @PathVariable("categoryId") Long categoryId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return noteService.getNotes(accountId, workspaceId, eventId, categoryId);
    }

    // 単体取得
    @GetMapping("/{noteId}")
    public NoteResponse getNoteById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId,
            @PathVariable("categoryId") Long categoryId,
            @PathVariable("noteId") Long noteId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return noteService.getNoteById(accountId, workspaceId, eventId, categoryId, noteId);
    }

    // 作成
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse createNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId,
            @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody NoteCreateRequest request
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return noteService.createNote(accountId, workspaceId, eventId, categoryId, request);
    }

    // 削除
    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId,
            @PathVariable("categoryId") Long categoryId,
            @PathVariable("noteId") Long noteId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        noteService.deleteNote(accountId, workspaceId, eventId, categoryId, noteId);
    }

    // 本文を更新し、更新後のNoteを返す。
    @PatchMapping("/{noteId}")
    public NoteResponse updateNote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId,
            @PathVariable("categoryId") Long categoryId,
            @PathVariable("noteId") Long noteId,
            @Valid @RequestBody NoteUpdateRequest request
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return noteService.updateNote(accountId, workspaceId, eventId, categoryId, noteId, request);
    }
}
