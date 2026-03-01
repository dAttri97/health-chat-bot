package com.curelink.test.dattri.auth;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Validates API key from header and sets authentication in context.
 * Reads key from X-API-Key or Authorization: ApiKey &lt;key&gt; or Authorization: Bearer &lt;key&gt;.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_X_API_KEY = "X-API-Key";
    public static final String AUTH_SCHEME_API_KEY = "ApiKey";
    public static final String AUTH_SCHEME_BEARER = "Bearer";

    private final String validApiKey;
    private final ApiKeyToSessionIdResolver sessionIdResolver;

    public ApiKeyAuthenticationFilter(String validApiKey, ApiKeyToSessionIdResolver sessionIdResolver) {
        this.validApiKey = validApiKey != null ? validApiKey : "";
        this.sessionIdResolver = sessionIdResolver != null ? sessionIdResolver : apiKey -> apiKey;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<String> apiKeyOpt = extractApiKey(request);

        if (apiKeyOpt.isPresent() && isValid(apiKeyOpt.get())) {
            String apiKey = apiKeyOpt.get();
            String sessionId = sessionIdResolver.resolve(apiKey);
            ApiKeyAuthenticationToken auth = new ApiKeyAuthenticationToken(apiKey, sessionId);
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractApiKey(HttpServletRequest request) {
        String header = request.getHeader(HEADER_X_API_KEY);
        if (header != null && !header.isBlank()) {
            return Optional.of(header.trim());
        }
        String auth = request.getHeader("Authorization");
        if (auth != null && (auth.startsWith(AUTH_SCHEME_BEARER + " ") || auth.startsWith(AUTH_SCHEME_API_KEY + " "))) {
            String key = auth.substring(auth.indexOf(' ') + 1).trim();
            return key.isEmpty() ? Optional.empty() : Optional.of(key);
        }
        return Optional.empty();
    }

    private boolean isValid(String key) {
        if (validApiKey.isEmpty()) {
            return true;
        }
        return validApiKey.equals(key);
    }

    @FunctionalInterface
    public interface ApiKeyToSessionIdResolver {
        String resolve(String apiKey);
    }
}
