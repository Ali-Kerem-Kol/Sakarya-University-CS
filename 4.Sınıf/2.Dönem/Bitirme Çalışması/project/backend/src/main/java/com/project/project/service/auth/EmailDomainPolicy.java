package com.project.project.service.auth;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.project.project.config.exception.InvalidEmailDomainException;
import com.project.project.entity.Role;

/**
 * Centralizes role-based email domain normalization and validation.
 */
@Service
public class EmailDomainPolicy {

    public static final String USER_DOMAIN = "@ogr.sakarya.edu.tr";
    public static final String ADMIN_DOMAIN = "@32bit.com.tr";

    public String normalize(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void assertForRole(String normalizedEmail, Role role) {
        if (role == Role.ADMIN) {
            if (!normalizedEmail.endsWith(ADMIN_DOMAIN)) {
                throw new InvalidEmailDomainException("ADMIN email must end with " + ADMIN_DOMAIN);
            }
            return;
        }
        if (!normalizedEmail.endsWith(USER_DOMAIN)) {
            throw new InvalidEmailDomainException("Email must end with " + USER_DOMAIN);
        }
    }
}
