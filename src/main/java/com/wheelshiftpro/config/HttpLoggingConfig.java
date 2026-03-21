package com.wheelshiftpro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Configures HTTP request logging using Spring's built-in CommonsRequestLoggingFilter.
 *
 * To enable logging, set the following in your profile properties:
 *   logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter=DEBUG
 *
 * This logs the incoming request URI, headers, and payload at DEBUG level —
 * no output appears unless the logger level is explicitly set to DEBUG.
 */
@Configuration
public class HttpLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(2000);
        filter.setIncludeClientInfo(true);
        filter.setHeaderPredicate(name ->
            !name.equalsIgnoreCase("Authorization") &&
            !name.equalsIgnoreCase("Cookie") &&
            !name.equalsIgnoreCase("Set-Cookie")
        );
        return filter;
    }
}
