package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.WorkspaceInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.UUID;

import java.util.Optional;

@Repository
public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInviteEntity, Long> {
    Optional<WorkspaceInviteEntity> findByCode(String code);
    @Modifying
    @Query("""
            UPDATE WorkspaceInviteEntity invite
            SET invite.expiresAt = :now
            WHERE invite.workspace.id = :workspaceId AND invite.expiresAt > :now
            """)
    int expireByWorkspaceId(@Param("workspaceId") UUID workspaceId,
                           @Param("now") OffsetDateTime now);
}
