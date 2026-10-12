package com.kokorono_note.kokorono_note.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.config.import=optional:file:.env[.properties]",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/kokorono_note",
        "spring.datasource.username=kokorono_note",
        "spring.datasource.password=${POSTGRES_PASSWORD}",
        "app.frontend-url=http://localhost:3000"
})
@ActiveProfiles(value = "prod", inheritProfiles = false)
class ProdSecurityFilterChainTests extends SecurityFilterChainTests {
}
