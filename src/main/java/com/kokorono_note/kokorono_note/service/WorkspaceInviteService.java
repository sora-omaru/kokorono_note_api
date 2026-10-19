package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.dto.response.InviteResponse;
import com.kokorono_note.kokorono_note.dto.response.WorkspaceResponse;
import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceAccountEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceEntity;
import com.kokorono_note.kokorono_note.entity.WorkspaceInviteEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceAccountRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceInviteRepository;
import com.kokorono_note.kokorono_note.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
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
    final AccountRepository accountRepository;
    final WorkspaceAccountRepository workspaceAccountRepository;
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
        invite.setCode(generateUniqueInviteCode());
        invite.setExpiresAt(OffsetDateTime.now().plusMinutes(30));

        WorkspaceInviteEntity saved = workspaceInviteRepository.save(invite);

        return new InviteResponse(
                saved.getCode(), saved.getExpiresAt()
        );
    }

    @Transactional
    public WorkspaceResponse joinWorkspace(UUID accountId, String code) {


        WorkspaceInviteEntity invite = workspaceInviteRepository.findByCode(code).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid invite code"
        ));


        //有効期限確認
        if (!invite.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invite code has expired"
            );
        }

        WorkspaceEntity workspace = invite.getWorkspace();

        //すでに登録済みかを確認する
        if (workspaceAccountRepository.existsByWorkspace_IdAndAccount_Id(
                workspace.getId(), accountId
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Already joined workspace"
            );
        }

        //アカウント情報を取得
        AccountEntity account = accountRepository.findById(accountId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Not found account"
        ));


        //登録されていない場合、WorkspaceAccountに登録する
        WorkspaceAccountEntity membership = new WorkspaceAccountEntity();

        membership.setAccount(account);
        membership.setWorkspace(workspace);

        workspaceAccountRepository.save(membership);

        //登録されたWorkspace情報を返す
        return new WorkspaceResponse(workspace.getId(), workspace.getName());
    }

    ;

    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(CHARS.length());
            code.append(CHARS.charAt(index));
        }

        return code.toString();
    }

    //コードの重複を確認し、新しいのを発行する。
    private String generateUniqueInviteCode() {
        final int MAX_ATTEMPTS = 5;

        for (int count = 0; count < MAX_ATTEMPTS; count++) {
            String code = generateInviteCode();

            if (workspaceInviteRepository.findByCode(code).isEmpty()) {
                return code;
            }
        }

        throw new IllegalStateException(
                "Failed to generate unique invite code"
        );
    }
}
