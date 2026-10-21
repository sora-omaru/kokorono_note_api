package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findAllByWorkspaceIdOrderByCreatedAtAscIdAsc(UUID workspaceId);

    Optional<EventEntity> findByIdAndWorkspaceId(Long eventId, UUID workspaceId);
}
