package com.kokorono_note.kokorono_note.service;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountEntity findOrCreateAccount(
            String googleSub,
            String displayName
    ){
       return  accountRepository.findByGoogleSub(googleSub)
               .orElseGet(()->{
                   AccountEntity account = new AccountEntity();
                  account.setGoogleSub(googleSub);
                  account.setDisplayName(displayName);

                  return accountRepository.save(account);
               });
    }
}
