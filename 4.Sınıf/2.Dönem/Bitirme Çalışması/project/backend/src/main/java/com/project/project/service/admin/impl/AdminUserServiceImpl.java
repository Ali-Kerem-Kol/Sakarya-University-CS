package com.project.project.service.admin.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.ConflictException;
import com.project.project.config.exception.InvalidPaginationException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.config.exception.BadRequestException;
import com.project.project.dto.admin.AdminUserCreateResponse;
import com.project.project.dto.admin.AdminUserSummaryResponse;
import com.project.project.dto.admin.CreateAdminUserRequest;
import com.project.project.entity.Role;
import com.project.project.dto.user.UserProfileResponse;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserProfile;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.service.admin.AdminUserService;
import com.project.project.service.auth.EmailDomainPolicy;
import com.project.project.util.UserColorResolver;

/**
 * Implements admin operations for browsing user profiles.
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailDomainPolicy emailDomainPolicy;

    public AdminUserServiceImpl(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            EmailDomainPolicy emailDomainPolicy
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailDomainPolicy = emailDomainPolicy;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserSummaryResponse> listUsers(Pageable pageable) {
        validatePageable(pageable, allowedSortProperties());
        return userAccountRepository.findAll(pageable).map(this::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toResponse(account);
    }

    @Override
    @Transactional
    public AdminUserSummaryResponse updateUserColor(Long userId, String color) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String normalizedColor = UserColorResolver.normalizeHexOrNull(color);
        if (color != null && !color.trim().isEmpty() && normalizedColor == null) {
            throw new BadRequestException("color must be a valid hex value like #AABBCC");
        }

        account.setPreferredColor(normalizedColor);
        return toSummaryResponse(userAccountRepository.save(account));
    }

    @Override
    @Transactional
    public AdminUserCreateResponse createAdminUser(CreateAdminUserRequest request) {
        String normalizedEmail = emailDomainPolicy.normalize(request.email());
        emailDomainPolicy.assertForRole(normalizedEmail, Role.ADMIN);
        userAccountRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            throw new ConflictException("Email already registered");
        });

        UserAccount account = new UserAccount();
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(Role.ADMIN);
        account.setEnabled(true);
        account.setEmailVerified(true);
        UserAccount savedAccount = userAccountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUserAccount(savedAccount);
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        UserProfile savedProfile = userProfileRepository.save(profile);

        return new AdminUserCreateResponse(
                savedAccount.getId(),
                savedAccount.getEmail(),
                savedAccount.getRole().name(),
                savedAccount.isEnabled(),
                savedAccount.isEmailVerified(),
                savedProfile.getFirstName(),
                savedProfile.getLastName()
        );
    }

    private UserProfileResponse toResponse(UserAccount account) {
        UserProfile profile = account.getProfile();
        com.project.project.entity.Document cv = profile != null ? profile.getCvDocument() : null;
        boolean hasCv = cv != null;
        String cvFileName = hasCv ? cv.getOriginalFileName() : null;
        java.time.Instant cvUploadedAt = hasCv ? cv.getUploadedAt() : null;
        String cvDownloadUrl = hasCv
                ? "/admin/users/" + account.getId() + "/documents/" + cv.getId() + "/download"
                : null;
        if (profile == null) {
            return new UserProfileResponse(
                    account.getId(),
                    account.getEmail(),
                    null,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    null
            );
        }
        return new UserProfileResponse(
                profile.getId(),
                account.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhoneNumber(),
                profile.getDateOfBirth(),
                hasCv,
                cvFileName,
                cvUploadedAt,
                cvDownloadUrl
        );
    }

    private AdminUserSummaryResponse toSummaryResponse(UserAccount account) {
        return new AdminUserSummaryResponse(
                account.getId(),
                account.getEmail(),
                account.getRole().name(),
                account.isEnabled(),
                UserColorResolver.resolveDisplayColor(account.getId(), account.getPreferredColor())
        );
    }

    private List<String> allowedSortProperties() {
        return List.of("id", "createdAt", "email", "role", "enabled");
    }

    private void validatePageable(Pageable pageable, List<String> allowedProperties) {
        if (pageable.getPageNumber() < 0) {
            throw new InvalidPaginationException("Page index must not be negative");
        }
        if (pageable.getPageSize() < 1) {
            throw new InvalidPaginationException("Page size must be >= 1");
        }
        pageable.getSort().forEach(order -> {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new InvalidPaginationException("Invalid sort property: " + order.getProperty());
            }
        });
    }
}
