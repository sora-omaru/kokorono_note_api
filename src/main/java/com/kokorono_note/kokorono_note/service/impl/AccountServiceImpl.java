package com.kokorono_note.kokorono_note.service.impl;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import com.kokorono_note.kokorono_note.service.AccountService;
import com.kokorono_note.kokorono_note.service.JwtService;
import com.kokorono_note.kokorono_note.service.oauth.TemporaryAuthCodeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TemporaryAuthCodeService temporaryAuthCodeService;
    private final JwtService jwtService;

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

    @Override
    public  String exchangeCodeForAccessToken(String code){
        UUID accountId = temporaryAuthCodeService.consume(code);

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(()->new IllegalStateException("Account Not Found"));

        return jwtService.generateAccessToken(account);
    }
}
