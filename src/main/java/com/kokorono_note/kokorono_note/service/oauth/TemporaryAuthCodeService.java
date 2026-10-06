package com.kokorono_note.kokorono_note.service.oauth;

import java.util.UUID;

public interface TemporaryAuthCodeService {
    String generate(UUID accountId);

    UUID consume(String code);
}
