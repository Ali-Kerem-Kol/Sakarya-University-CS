package com.project.project.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Verifies critical infrastructure dependencies at startup and fails fast when
 * they are not reachable.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StartupDependencyVerifier implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupDependencyVerifier.class);

    private final DataSource dataSource;
    private final JavaMailSender javaMailSender;

    public StartupDependencyVerifier(
            DataSource dataSource,
            JavaMailSender javaMailSender) {
        this.dataSource = dataSource;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void run(ApplicationArguments args) {
        verifyDatabaseConnection();
        verifyMailConnection();
    }

    private void verifyDatabaseConnection() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            logger.info("Startup check: DB connection ok");
        } catch (SQLException ex) {
            throw new IllegalStateException("Startup check failed: database is not reachable", ex);
        }
    }

    private void verifyMailConnection() {
        if (!isMailVerificationEnabled()) {
            logger.info("Startup check: mail verification skipped (APP_STARTUP_VERIFY_MAIL=false)");
            return;
        }

        try {
            if (javaMailSender instanceof JavaMailSenderImpl sender) {
                sender.testConnection();
                logger.info("Startup check: mail ok (host={}, port={})", sender.getHost(), sender.getPort());
                return;
            }
            logger.info("Startup check: mail sender available");
        } catch (Exception ex) {
            throw new IllegalStateException("Startup check failed: mail server is not reachable", ex);
        }
    }

    private boolean isMailVerificationEnabled() {
        String value = System.getenv("APP_STARTUP_VERIFY_MAIL");
        if (!StringUtils.hasText(value)) {
            return true;
        }
        return Boolean.parseBoolean(value);
    }
}
