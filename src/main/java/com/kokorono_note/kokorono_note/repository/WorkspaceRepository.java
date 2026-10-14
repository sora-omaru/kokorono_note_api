package com.kokorono_note.kokorono_note.repository;

import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, UUID> {
    @Query("""
                  SELECT workspace
                  FROM WorkspaceEntity workspace
                  JOIN WorkspaceAccountEntity workspaceAccount
                              ON workspaceAccount.workspace.id = workspace.id
                  WHERE workspaceAccount.account.id = :accountId
            """)
    List<WorkspaceEntity> findAllByAccountId(@Param("accountId") UUID accountId);
}
