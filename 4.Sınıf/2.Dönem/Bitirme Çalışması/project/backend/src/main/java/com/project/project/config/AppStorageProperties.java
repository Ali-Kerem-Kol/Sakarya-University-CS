package com.project.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Holds storage configuration for uploaded documents.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class AppStorageProperties {

    private String root = "./storage";

    private long maxFileSizeBytes = 52428800;
}
