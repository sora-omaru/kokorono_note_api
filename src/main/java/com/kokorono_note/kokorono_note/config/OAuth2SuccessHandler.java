package com.kokorono_note.kokorono_note.config;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.service.AccountService;
import com.kokorono_note.kokorono_note.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AccountService accountService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String googleSub = oAuth2User.getAttribute("sub");
        if (googleSub == null) {
            throw new IllegalStateException("Google sub is missing");
        }
        String displayName = oAuth2User.getAttribute("name");

        AccountEntity account =
                accountService.findOrCreateAccount(googleSub, displayName);

        String accessToken = jwtService.generateAccessToken(account);


    }
}
