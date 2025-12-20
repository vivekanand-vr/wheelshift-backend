package com.wheelshiftpro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for JPA Auditing.
 * Enables automatic population of created and updated timestamps.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
