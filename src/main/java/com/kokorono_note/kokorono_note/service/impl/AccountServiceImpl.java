package com.kokorono_note.kokorono_note.service.impl;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import com.kokorono_note.kokorono_note.service.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    @Override
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
