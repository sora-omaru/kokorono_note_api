package com.kokorono_note.kokorono_note.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

class FrontendLoginFlowFilterTests {
    @Test
    void requiresFlowAndBindsItToTheOAuthSession() throws Exception {
        var filter = new FrontendLoginFlowFilter();
        var request = new MockHttpServletRequest();
        request.setServletPath("/oauth2/authorization/google");
        var rejected = new MockHttpServletResponse();
        filter.doFilter(request, rejected, (req, res) -> fail("Must not start OAuth without flow"));
        assertEquals(400, rejected.getStatus());
        request.addParameter("flow", "a".repeat(64));
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});
        assertEquals("a".repeat(64), request.getSession().getAttribute(FrontendLoginFlowFilter.SESSION_ATTRIBUTE));
    }
}
