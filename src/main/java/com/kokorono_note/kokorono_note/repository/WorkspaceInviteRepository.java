package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.WorkspaceInviteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceInviteRepository extends JpaRepository<WorkspaceInviteEntity, Long> {
    Optional<WorkspaceInviteEntity> findByCode(String code);
}
