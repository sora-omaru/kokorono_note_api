package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.response.InviteResponse;
import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceInviteEntity;
import com.kokorono_note.kokorono_note.repository.WorkspaceInviteRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceInviteService {
    final WorkspaceRepository workspaceRepository;
    final WorkspaceInviteRepository workspaceInviteRepository;

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final SecureRandom RANDOM = new SecureRandom();


    public InviteResponse createInvite(UUID accountId, UUID workspaceId) {
        WorkspaceEntity workspace = workspaceRepository.findByIdAndAccountId(workspaceId, accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Workspace not found"
                ));

        WorkspaceInviteEntity invite = new WorkspaceInviteEntity();
        invite.setWorkspace(workspace);
        invite.setCode(generateInviteCode());
        invite.setExpiresAt(OffsetDateTime.now().plusMinutes(30));

        WorkspaceInviteEntity saved = workspaceInviteRepository.save(invite);

        return new InviteResponse(
                saved.getCode(), saved.getExpiresAt()
        );
    }

    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(CHARS.length());
            code.append(CHARS.charAt(index));
        }

        return code.toString();
    }
}
