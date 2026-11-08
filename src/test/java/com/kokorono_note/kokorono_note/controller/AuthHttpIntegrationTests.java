package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.entity.AccountEntity;
import com.kokorono_note.kokorono_note.repository.AccountRepository;
import com.kokorono_note.kokorono_note.repository.RefreshTokenRepository;
import com.kokorono_note.kokorono_note.service.oauth.TemporaryAuthCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthHttpIntegrationTests {
    @LocalServerPort int port;
    @Autowired AccountRepository accounts;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired TemporaryAuthCodeService codes;
    @Autowired PlatformTransactionManager transactions;
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void realCodeExchangeCsrfAndRefreshRotation() throws Exception {
        var account = new AccountEntity();
        account.setGoogleSub("http-test-" + UUID.randomUUID());
        account = accounts.saveAndFlush(account);
        final UUID id = account.getId();
        var jar = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        var client = HttpClient.newBuilder().cookieHandler(jar).build();
        String base = "http://localhost:" + port;
        try {
            String code = codes.generate(id);
            var exchange = HttpRequest.newBuilder(URI.create(base + "/auth/token"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"code\":\"" + code + "\"}")).build();
            var tokens = client.send(exchange, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, tokens.statusCode());
            assertFalse(tokens.body().contains("refreshToken"));
            String access = json.readTree(tokens.body()).get("accessToken").asText();
            assertEquals(400, client.send(exchange, HttpResponse.BodyHandlers.discarding()).statusCode());
            var list = HttpRequest.newBuilder(URI.create(base + "/api/v1/workspaces"))
                    .header("Authorization", "Bearer " + access).GET().build();
            assertEquals(200, client.send(list, HttpResponse.BodyHandlers.discarding()).statusCode());

            var refresh = HttpRequest.newBuilder(URI.create(base + "/auth/refresh"))
                    .POST(HttpRequest.BodyPublishers.noBody());
            assertEquals(403, client.send(refresh.build(), HttpResponse.BodyHandlers.discarding()).statusCode());
            var csrfResult = client.send(HttpRequest.newBuilder(URI.create(base + "/auth/csrf")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            var csrf = json.readTree(csrfResult.body());
            String header = csrf.get("headerName").asText();
            String token = csrf.get("token").asText();
            assertTrue(jar.getCookieStore().getCookies().stream().anyMatch(c -> c.getName().equals("JSESSIONID")));
            assertEquals(403, client.send(refresh.copy().header(header, "wrong").build(), HttpResponse.BodyHandlers.discarding()).statusCode());
            String oldRefresh = jar.getCookieStore().getCookies().stream()
                    .filter(c -> c.getName().equals("refreshToken")).findFirst().orElseThrow().getValue();
            assertEquals(200, client.send(refresh.copy().header(header, token).build(), HttpResponse.BodyHandlers.discarding()).statusCode());
            String newRefresh = jar.getCookieStore().getCookies().stream()
                    .filter(c -> c.getName().equals("refreshToken")).findFirst().orElseThrow().getValue();
            assertNotEquals(oldRefresh, newRefresh);
            var noCookies = HttpClient.newHttpClient();
            String backendSession = jar.getCookieStore().getCookies().stream()
                    .filter(c -> c.getName().equals("JSESSIONID")).findFirst().orElseThrow().getValue();
            var reused = refresh.copy().header(header, token)
                    .header("Cookie", "JSESSIONID=" + backendSession + "; refreshToken=" + oldRefresh).build();
            assertEquals(401, noCookies.send(reused, HttpResponse.BodyHandlers.discarding()).statusCode());
        } finally {
            new TransactionTemplate(transactions).executeWithoutResult(status -> {
                refreshTokens.deleteAllByAccount_Id(id);
                accounts.deleteById(id);
            });
        }
    }
}
