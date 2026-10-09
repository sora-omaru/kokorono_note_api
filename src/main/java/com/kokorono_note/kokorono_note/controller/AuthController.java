package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.AuthCodeRequest;
import com.kokorono_note.kokorono_note.dto.response.AccessTokenResponse;
import com.kokorono_note.kokorono_note.dto.response.TokenResponse;
import com.kokorono_note.kokorono_note.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/token")
    public ResponseEntity<AccessTokenResponse> exchangeCodeForAccessToken(@RequestBody AuthCodeRequest request) {
        try {

            //Token取得
            TokenResponse tokens = authService.exchangeCodeForAccessToken(request.code());
            //取得したTokenからRefreshTokenの切り出し
            ResponseCookie cookie = ResponseCookie.from(
                            "refreshToken",
                            tokens.refreshToken()
                    )
                    .httpOnly(true)
                    .secure(false) // ローカルHTTP開発用
                    .sameSite("Lax")
                    .path("/auth")
                    .maxAge(Duration.ofDays(30))
                    .build();

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new AccessTokenResponse(tokens.accessToken()));

        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid authentication code",
                    exception
            );
        }
    }
}
