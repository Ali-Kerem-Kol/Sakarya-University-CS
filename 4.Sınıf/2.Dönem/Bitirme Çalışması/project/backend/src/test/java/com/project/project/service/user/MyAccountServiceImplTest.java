package com.project.project.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.auth.EmailDomainPolicy;
import com.project.project.service.user.impl.MyAccountServiceImpl;

@ExtendWith(MockitoExtension.class)
class MyAccountServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailDomainPolicy emailDomainPolicy;

    @InjectMocks
    private MyAccountServiceImpl myAccountService;

    @Test
    void updateEmailUserRoleIsForbidden() {
        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setRole(Role.USER);
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> myAccountService.updateEmail(10L, "student@ogr.sakarya.edu.tr"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("USER_EMAIL_UPDATE_FORBIDDEN");

        verify(userAccountRepository, never()).save(user);
    }

    @Test
    void updateEmailAdminRoleIsAllowed() {
        UserAccount admin = new UserAccount();
        admin.setId(20L);
        admin.setRole(Role.ADMIN);
        admin.setEmail("old@32bit.com.tr");
        when(userAccountRepository.findById(20L)).thenReturn(Optional.of(admin));
        when(emailDomainPolicy.normalize("new-admin@32bit.com.tr")).thenReturn("new-admin@32bit.com.tr");
        when(userAccountRepository.findByEmailIgnoreCase("new-admin@32bit.com.tr")).thenReturn(Optional.empty());
        when(userAccountRepository.save(admin)).thenReturn(admin);

        var response = myAccountService.updateEmail(20L, "new-admin@32bit.com.tr");

        assertThat(response.email()).isEqualTo("new-admin@32bit.com.tr");
        verify(emailDomainPolicy).assertForRole("new-admin@32bit.com.tr", Role.ADMIN);
    }
}

