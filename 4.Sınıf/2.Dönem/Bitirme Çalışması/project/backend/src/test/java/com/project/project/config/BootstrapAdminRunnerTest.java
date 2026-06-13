package com.project.project.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.UserAccountRepository;

/**
 * Verifies bootstrap admin creation behavior.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "BOOTSTRAP_ADMIN_EMAIL=admin@test.com",
        "BOOTSTRAP_ADMIN_PASSWORD=secret12345"
})
class BootstrapAdminRunnerTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private BootstrapAdminRunner bootstrapAdminRunner;

    @Test
    void createsAdminWhenMissing() {
        UserAccount admin = userAccountRepository.findByEmail("admin@test.com").orElse(null);
        assertThat(admin).isNotNull();
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getPasswordHash()).isNotEqualTo("secret12345");
    }

    @Test
    void doesNotCreateDuplicateAdmin() throws Exception {
        long before = userAccountRepository.countByRole(Role.ADMIN);
        bootstrapAdminRunner.run(new DefaultApplicationArguments(new String[0]));
        long after = userAccountRepository.countByRole(Role.ADMIN);
        assertThat(after).isEqualTo(before);
    }
}
