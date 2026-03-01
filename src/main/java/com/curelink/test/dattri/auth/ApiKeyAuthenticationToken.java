package com.curelink.test.dattri.auth;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Authentication token for API-key–based auth.
 * Principal is the session id (derived from API key); credentials are the raw API key.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String apiKey;
    private final String sessionId;

    public ApiKeyAuthenticationToken(String apiKey, String sessionId) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.apiKey = apiKey;
        this.sessionId = sessionId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
