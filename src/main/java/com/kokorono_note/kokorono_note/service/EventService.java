package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.request.EventCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.EventResponse;
import com.kokorono_note.kokorono_note.entity.EventEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import com.kokorono_note.kokorono_note.repository.EventRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EventService {
    final WorkspaceRepository workspaceRepository;
    final EventRepository eventRepository;

    public EventResponse createEvent(
            UUID accountId,
            UUID workspaceId,
            EventCreateRequest request
    ) {
        WorkspaceEntity workspace =
                workspaceRepository.findByIdAndAccountId(
                        workspaceId, accountId
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Workspace not found"
                ));

        EventEntity event = new EventEntity();
        event.setWorkspace(workspace);
        event.setName(request.name().trim());

        EventEntity saved = eventRepository.save(event);

        return new EventResponse(saved.getId(), saved.getName());
    }
}
