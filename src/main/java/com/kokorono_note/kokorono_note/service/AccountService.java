package com.kokorono_note.kokorono_note.service;


import com.kokorono_note.kokorono_note.entity.AccountEntity;

public interface AccountService {
    AccountEntity findOrCreateAccount(String googleSub, String displayName);

}
