package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.EventCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.EventResponse;
import com.kokorono_note.kokorono_note.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<EventResponse> getEvents(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return eventService.getEvents(accountId, workspaceId);
    }

    @GetMapping("/{eventId}")
    public EventResponse getEventById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @PathVariable("eventId") Long eventId
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return eventService.getEventById(accountId, workspaceId, eventId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("workspaceId") UUID workspaceId,
            @Valid @RequestBody EventCreateRequest request
    ) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return eventService.createEvent(accountId, workspaceId, request);
    }
}
