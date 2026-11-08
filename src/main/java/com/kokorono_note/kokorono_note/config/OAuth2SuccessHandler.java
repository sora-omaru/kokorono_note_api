package com.kokorono_note.kokorono_note.config;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.service.AccountService;
import com.kokorono_note.kokorono_note.service.oauth.TemporaryAuthCodeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AccountService accountService;
    private final TemporaryAuthCodeService temporaryAuthCodeService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var session = request.getSession(false);
        String flow = session == null ? null : (String) session.getAttribute(FrontendLoginFlowFilter.SESSION_ATTRIBUTE);
        if (session != null) session.removeAttribute(FrontendLoginFlowFilter.SESSION_ATTRIBUTE);
        if (flow == null || !flow.matches("[a-f0-9]{64}")) {
            response.sendRedirect(frontendUrl + "/login?error=oauth");
            return;
        }
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String googleSub = oAuth2User.getAttribute("sub");
        if (googleSub == null) {
            throw new IllegalStateException("Google sub is missing");
        }
        String displayName = oAuth2User.getAttribute("name");

        AccountEntity account =
                accountService.findOrCreateAccount(googleSub, displayName);

        String code = temporaryAuthCodeService.generate(account.getId());

        String redirectUrl =
                UriComponentsBuilder
                        .fromUriString(frontendUrl)
                        .path("/oauth/callback")
                        .queryParam("code", code)
                        .queryParam("flow", flow)
                        .build()
                        .toUriString();
//Google認証終了時にフロントへリダイレクト
        response.sendRedirect(redirectUrl);

    }
}
