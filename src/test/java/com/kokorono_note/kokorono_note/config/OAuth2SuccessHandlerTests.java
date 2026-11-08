package com.kokorono_note.kokorono_note.config;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.service.AccountService;
import com.kokorono_note.kokorono_note.service.oauth.TemporaryAuthCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OAuth2SuccessHandlerTests {
    @Test
    void returnsCodeAndFlowOnlyForTheBrowserThatStartedOAuth() throws Exception {
        var accounts = mock(AccountService.class);
        var codes = mock(TemporaryAuthCodeService.class);
        var handler = new OAuth2SuccessHandler(accounts, codes);
        ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:3000");
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var user = new DefaultOAuth2User(authorities, Map.of("sub", "google-user", "name", "User"), "sub");
        var auth = new OAuth2AuthenticationToken(user, authorities, "google");
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, auth);
        assertEquals("http://localhost:3000/login?error=oauth", response.getRedirectedUrl());
        verifyNoInteractions(accounts, codes);
        request.getSession().setAttribute(FrontendLoginFlowFilter.SESSION_ATTRIBUTE, "a".repeat(64));
        var account = new AccountEntity();
        account.setId(UUID.randomUUID());
        when(accounts.findOrCreateAccount("google-user", "User")).thenReturn(account);
        when(codes.generate(account.getId())).thenReturn("one-time-code");
        response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, auth);
        assertEquals("http://localhost:3000/oauth/callback?code=one-time-code&flow=" + "a".repeat(64), response.getRedirectedUrl());
        assertNull(request.getSession().getAttribute(FrontendLoginFlowFilter.SESSION_ATTRIBUTE));
    }
}
