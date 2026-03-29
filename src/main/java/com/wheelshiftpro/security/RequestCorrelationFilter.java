package com.wheelshiftpro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Servlet filter that assigns a unique correlation ID to every incoming request.
 *
 * <p>The ID is taken from the {@code X-Request-Id} header if present (useful
 * for tracing requests across services), or generated as a random UUID otherwise.
 *
 * <p>The ID is:
 * <ul>
 *   <li>Stored in SLF4J MDC under key {@code requestId} — appears in every log line</li>
 *   <li>Returned to the caller via the {@code X-Request-Id} response header</li>
 * </ul>
 *
 * <p>Use in log patterns: {@code %X{requestId}}
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = Optional.ofNullable(request.getHeader(REQUEST_ID_HEADER))
                .filter(h -> !h.isBlank())
                .orElse(UUID.randomUUID().toString());

        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear MDC to avoid leaking request IDs across thread-pool reuse
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }
}
