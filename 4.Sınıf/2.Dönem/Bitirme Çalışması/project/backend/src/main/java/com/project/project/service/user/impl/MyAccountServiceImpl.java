package com.project.project.service.user.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.BadRequestException;
import com.project.project.config.exception.ConflictException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.user.MyAccountResponse;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.auth.EmailDomainPolicy;
import com.project.project.service.user.MyAccountService;

/**
 * Implements email/password updates for authenticated users.
 */
@Service
public class MyAccountServiceImpl implements MyAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailDomainPolicy emailDomainPolicy;

    public MyAccountServiceImpl(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            EmailDomainPolicy emailDomainPolicy
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailDomainPolicy = emailDomainPolicy;
    }

    @Override
    @Transactional(readOnly = true)
    public MyAccountResponse getAccount(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new MyAccountResponse(user.getId(), user.getEmail(), user.getRole().name());
    }

    @Override
    @Transactional
    public MyAccountResponse updateEmail(Long userId, String rawEmail) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() == Role.USER) {
            throw new AccessDeniedException("USER_EMAIL_UPDATE_FORBIDDEN");
        }
        String normalizedEmail = emailDomainPolicy.normalize(rawEmail);
        emailDomainPolicy.assertForRole(normalizedEmail, user.getRole());
        userAccountRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            if (!existing.getId().equals(userId)) {
                throw new ConflictException("Email already registered");
            }
        });
        user.setEmail(normalizedEmail);
        UserAccount saved = userAccountRepository.save(user);
        return new MyAccountResponse(saved.getId(), saved.getEmail(), saved.getRole().name());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BadRequestException("CURRENT_PASSWORD_INVALID");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);
    }
}
