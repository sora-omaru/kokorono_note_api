package com.kokorono_note.kokorono_note.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Google OAuthのstateに加え、フロントで開始したログインと戻り先を結び付ける。 */
public class FrontendLoginFlowFilter extends OncePerRequestFilter {
    public static final String SESSION_ATTRIBUTE = "frontendLoginFlow";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (request.getServletPath().equals("/oauth2/authorization/google")) {
            String flow = request.getParameter("flow");
            if (flow == null || !flow.matches("[a-f0-9]{64}")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Start login from the frontend");
                return;
            }
            request.getSession().setAttribute(SESSION_ATTRIBUTE, flow);
        }
        chain.doFilter(request, response);
    }
}
