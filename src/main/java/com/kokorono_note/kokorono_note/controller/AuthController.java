package com.kokorono_note.kokorono_note.controller;

import com.kokorono_note.kokorono_note.dto.request.AuthCodeRequest;
import com.kokorono_note.kokorono_note.dto.response.AccessTokenResponse;
import com.kokorono_note.kokorono_note.dto.response.TokenResponse;
import com.kokorono_note.kokorono_note.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/token")
    public AccessTokenResponse exchangeCodeForAccessToken(@RequestBody AuthCodeRequest request) {
        try {
            TokenResponse accessToken = authService.exchangeCodeForAccessToken(request.code());
            return new AccessTokenResponse(accessToken);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid authentication code", exception);
        }
    }
}
