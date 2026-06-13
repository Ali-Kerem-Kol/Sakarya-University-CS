package com.project.project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserProfile;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.service.auth.EmailDomainPolicy;

/**
 * Creates an initial admin account when none exist and bootstrap credentials
 * are provided.
 */
@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapAdminRunner.class);
    private static final String DEFAULT_ADMIN_EMAIL = "admin@32bit.com.tr";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin12345!";

    private final Environment environment;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailDomainPolicy emailDomainPolicy;

    public BootstrapAdminRunner(
            Environment environment,
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            EmailDomainPolicy emailDomainPolicy) {
        this.environment = environment;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailDomainPolicy = emailDomainPolicy;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userAccountRepository.countByRole(Role.ADMIN) > 0) {
            return;
        }
        String email = emailDomainPolicy.normalize(environment.getProperty("SEED_ADMIN_EMAIL", DEFAULT_ADMIN_EMAIL));
        String password = environment.getProperty("SEED_ADMIN_PASSWORD", DEFAULT_ADMIN_PASSWORD);
        String firstName = environment.getProperty("SEED_ADMIN_FIRSTNAME", "System");
        String lastName = environment.getProperty("SEED_ADMIN_LASTNAME", "Administrator");

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            logger.warn("Admin seed skipped: SEED_ADMIN_EMAIL or SEED_ADMIN_PASSWORD is blank.");
            return;
        }
        try {
            emailDomainPolicy.assertForRole(email, Role.ADMIN);
        } catch (com.project.project.config.exception.InvalidEmailDomainException ex) {
            logger.warn("Seed admin email domain is invalid ({}). Falling back to default {}",
                    email, DEFAULT_ADMIN_EMAIL);
            email = DEFAULT_ADMIN_EMAIL;
        }

        UserAccount account = userAccountRepository.findByEmail(email).orElseGet(UserAccount::new);
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(Role.ADMIN);
        account.setEnabled(true);
        account.setEmailVerified(true);
        UserAccount saved = userAccountRepository.save(account);

        UserProfile profile = userProfileRepository.findByUserAccountId(saved.getId())
                .orElseGet(UserProfile::new);
        profile.setUserAccount(saved);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        userProfileRepository.save(profile);
        logger.info("Admin seeded: email={}", email);
    }
}
