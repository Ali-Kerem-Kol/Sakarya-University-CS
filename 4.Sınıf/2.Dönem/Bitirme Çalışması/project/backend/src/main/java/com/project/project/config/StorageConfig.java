package com.project.project.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables storage-related configuration properties.
 */
@Configuration
@EnableConfigurationProperties({AppStorageProperties.class, CorsProperties.class})
public class StorageConfig {
}
