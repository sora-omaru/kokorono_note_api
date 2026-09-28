package com.kokorono_note.kokorono_note;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceAccountEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceAccountId;
import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WorkspaceAccountEntityTests {
    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndLoadsMembershipUsingDerivedCompositeId() {
        AccountEntity account = new AccountEntity();
        account.setGoogleSub(UUID.randomUUID().toString());
        entityManager.persist(account);

        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setName("test workspace");
        entityManager.persist(workspace);

        WorkspaceAccountEntity membership = new WorkspaceAccountEntity();
        membership.setAccount(account);
        membership.setWorkspace(workspace);
        entityManager.persist(membership);
        entityManager.flush();
        entityManager.clear();

        WorkspaceAccountId id = new WorkspaceAccountId(account.getId(), workspace.getId());
        assertEquals(id, membership.getId());
        assertEquals(id.hashCode(), membership.getId().hashCode());
        WorkspaceAccountEntity loaded = entityManager.find(WorkspaceAccountEntity.class, id);
        assertNotNull(loaded);
        assertNotNull(loaded.getJoinedAt());
        assertEquals(account.getId(), loaded.getAccount().getId());
        assertEquals(workspace.getId(), loaded.getWorkspace().getId());

        entityManager.remove(loaded);
        entityManager.flush();
        entityManager.clear();
        assertNotNull(entityManager.find(AccountEntity.class, account.getId()));
        assertNotNull(entityManager.find(WorkspaceEntity.class, workspace.getId()));
    }

    @Test
    void preservesExplicitJoinedAt() {
        WorkspaceAccountEntity membership = new WorkspaceAccountEntity();
        OffsetDateTime joinedAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        membership.setJoinedAt(joinedAt);
        membership.prePersist();
        assertEquals(joinedAt, membership.getJoinedAt());
    }
}
