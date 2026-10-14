package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.request.WorkspaceCreateRequest;
import com.kokorono_note.kokorono_note.dto.response.WorkspaceResponse;
import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceAccountEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceAccountRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    final AccountRepository accountRepository;
    final WorkspaceRepository workspaceRepository;
    final WorkspaceAccountRepository workspaceAccountRepository;

    public WorkspaceResponse create(
            UUID accountId, WorkspaceCreateRequest request
    ) {
        //認証済みのアカウント
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Account not found"));

        //Workspace作成
        WorkspaceEntity workspace = new WorkspaceEntity();
        workspace.setName(request.name().trim());

        WorkspaceEntity saved = workspaceRepository.save(workspace);

        //WorkspaceAccountにも設定
        WorkspaceAccountEntity membership = new WorkspaceAccountEntity();
        membership.setWorkspace(saved);
        membership.setAccount(account);

        workspaceAccountRepository.save(membership);

        return new WorkspaceResponse(saved.getId(), saved.getName());

    }

    @Transactional
    public List<WorkspaceResponse> getWorkspace(UUID accountId) {
        return workspaceRepository.findAllByAccountId(accountId).stream().map(
                        workspace -> new WorkspaceResponse(workspace.getId(), workspace.getName()))
                .toList();
    }
}
