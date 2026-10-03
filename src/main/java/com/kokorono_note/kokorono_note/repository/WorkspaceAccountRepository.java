package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.WorkspaceAccountEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceAccountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceAccountRepository extends JpaRepository<WorkspaceAccountEntity, WorkspaceAccountId> {

    boolean exitsByWorkspace_IdAndAccount_Id(UUID workspaceId, UUID accountID);

    List<WorkspaceAccountEntity> findByAccount_Id(UUID accountId);
}
