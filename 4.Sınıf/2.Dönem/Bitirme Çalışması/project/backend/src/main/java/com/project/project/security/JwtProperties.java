package com.project.project.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Holds JWT configuration values loaded from application properties.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret = "change-me-change-me-change-me-change-me";

    private String issuer = "project";

    private long accessTokenMinutes = 60;
}
