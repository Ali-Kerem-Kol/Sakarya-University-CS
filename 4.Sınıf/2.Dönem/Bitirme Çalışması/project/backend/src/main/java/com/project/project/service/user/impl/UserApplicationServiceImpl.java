package com.project.project.service.user.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.DuplicateApplicationException;
import com.project.project.config.exception.InvalidPaginationException;
import com.project.project.config.exception.InvalidStatusTransitionException;
import com.project.project.config.exception.MissingAvailabilityException;
import com.project.project.config.exception.MissingCvException;
import com.project.project.config.exception.MissingProfileException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.user.ApplicationListResponse;
import com.project.project.dto.user.ApplicationResponse;
import com.project.project.dto.user.CreateApplicationRequest;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserApplication;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.entity.UserProfile;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserApplicationRepository;
import com.project.project.repository.UserAvailabilitySlotRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.service.user.UserApplicationService;

/**
 * Implements application management for authenticated users and admins.
 */
@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    private final UserAccountRepository userAccountRepository;
    private final UserApplicationRepository userApplicationRepository;
    private final UserAvailabilitySlotRepository userAvailabilitySlotRepository;
    private final UserProfileRepository userProfileRepository;

    public UserApplicationServiceImpl(
            UserAccountRepository userAccountRepository,
            UserApplicationRepository userApplicationRepository,
            UserAvailabilitySlotRepository userAvailabilitySlotRepository,
            UserProfileRepository userProfileRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userApplicationRepository = userApplicationRepository;
        this.userAvailabilitySlotRepository = userAvailabilitySlotRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional
    public ApplicationResponse createForCurrentUser(Long userId, CreateApplicationRequest request) {
        UserAccount userAccount = getUserAccount(userId);
        boolean exists = userApplicationRepository.existsByUserAccountIdAndPositionKeyAndStatusIn(
                userId,
                request.positionKey(),
                activeStatuses()
        );
        if (exists) {
            throw new DuplicateApplicationException("Active application already exists for this position");
        }
        UserApplication application = new UserApplication();
        application.setUserAccount(userAccount);
        application.setPositionKey(request.positionKey());
        application.setMotivationText(request.motivationText());
        application.setStatus(UserApplicationStatus.DRAFT);
        application.setLastStatusChangedAt(Instant.now());
        UserApplication saved = userApplicationRepository.save(application);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationListResponse listForCurrentUser(Long userId) {
        getUserAccount(userId);
        List<UserApplication> applications = userApplicationRepository.findByUserAccountId(
                userId,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return new ApplicationListResponse(toResponses(applications));
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getForCurrentUser(Long userId, Long applicationId) {
        UserApplication application = userApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        if (!application.getUserAccount().getId().equals(userId)) {
            throw new AccessDeniedException("Forbidden");
        }
        return toResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse submitApplication(Long userId, Long applicationId) {
        UserApplication application = userApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        if (!application.getUserAccount().getId().equals(userId)) {
            throw new AccessDeniedException("Forbidden");
        }
        validateSubmitPrerequisites(userId);
        if (!isValidUserTransition(application.getStatus(), UserApplicationStatus.SUBMITTED)) {
            throw new InvalidStatusTransitionException("Invalid status transition");
        }
        application.setStatus(UserApplicationStatus.SUBMITTED);
        application.setLastStatusChangedAt(Instant.now());
        return toResponse(userApplicationRepository.save(application));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> adminList(UserApplicationStatus status, String positionKey, Pageable pageable) {
        validatePageable(pageable, allowedSortProperties());
        Page<UserApplication> applications = resolveAdminList(status, positionKey, pageable);
        return applications.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse adminGetById(Long applicationId) {
        UserApplication application = userApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        return toResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse adminUpdateStatus(Long applicationId, UserApplicationStatus status) {
        UserApplication application = userApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        if (!isValidAdminTransition(application.getStatus(), status)) {
            throw new InvalidStatusTransitionException("Invalid status transition");
        }
        application.setStatus(status);
        application.setLastStatusChangedAt(Instant.now());
        return toResponse(userApplicationRepository.save(application));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponse> adminExportApplications(
            UserApplicationStatus status,
            String positionKey,
            String query,
            Sort sort
    ) {
        validateSort(sort, allowedSortProperties());
        List<Long> userIds = resolveUserIds(query);
        if (query != null && !query.isBlank() && (userIds == null || userIds.isEmpty())) {
            return List.of();
        }
        List<UserApplication> applications = resolveAdminExportList(status, positionKey, userIds, sort);
        return toResponses(applications);
    }

    private Page<UserApplication> resolveAdminList(
            UserApplicationStatus status,
            String positionKey,
            Pageable pageable
    ) {
        if (status != null && positionKey != null && !positionKey.isBlank()) {
            return userApplicationRepository.findByStatusAndPositionKey(status, positionKey, pageable);
        }
        if (status != null) {
            return userApplicationRepository.findByStatus(status, pageable);
        }
        if (positionKey != null && !positionKey.isBlank()) {
            return userApplicationRepository.findByPositionKey(positionKey, pageable);
        }
        return userApplicationRepository.findAll(pageable);
    }

    private List<UserApplication> resolveAdminExportList(
            UserApplicationStatus status,
            String positionKey,
            List<Long> userIds,
            Sort sort
    ) {
        boolean hasUserFilter = userIds != null && !userIds.isEmpty();
        if (status != null && positionKey != null && !positionKey.isBlank()) {
            if (hasUserFilter) {
                return userApplicationRepository.findByUserAccountIdInAndStatusAndPositionKey(
                        userIds,
                        status,
                        positionKey,
                        sort
                );
            }
            return userApplicationRepository.findByStatusAndPositionKey(status, positionKey, sort);
        }
        if (status != null) {
            if (hasUserFilter) {
                return userApplicationRepository.findByUserAccountIdInAndStatus(userIds, status, sort);
            }
            return userApplicationRepository.findByStatus(status, sort);
        }
        if (positionKey != null && !positionKey.isBlank()) {
            if (hasUserFilter) {
                return userApplicationRepository.findByUserAccountIdInAndPositionKey(userIds, positionKey, sort);
            }
            return userApplicationRepository.findByPositionKey(positionKey, sort);
        }
        if (hasUserFilter) {
            return userApplicationRepository.findByUserAccountIdIn(userIds, sort);
        }
        return userApplicationRepository.findAll(sort);
    }

    private List<ApplicationResponse> toResponses(List<UserApplication> applications) {
        List<ApplicationResponse> responses = new ArrayList<>();
        for (UserApplication application : applications) {
            responses.add(toResponse(application));
        }
        return responses;
    }

    private ApplicationResponse toResponse(UserApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getUserAccount().getId(),
                application.getPositionKey(),
                application.getStatus(),
                application.getMotivationText(),
                application.getCreatedAt(),
                application.getLastStatusChangedAt()
        );
    }

    private UserAccount getUserAccount(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateSubmitPrerequisites(Long userId) {
        UserProfile profile = userProfileRepository.findByUserAccountId(userId)
                .orElseThrow(() -> new MissingProfileException("User profile is incomplete"));
        if (profile.getCvDocument() == null) {
            throw new MissingCvException("User must upload a CV before submitting");
        }
        if (!userAvailabilitySlotRepository.existsByProfileUserAccountId(userId)) {
            throw new MissingAvailabilityException("User must set availability before submitting");
        }
        if (profile.getFirstName() == null || profile.getFirstName().isBlank()
                || profile.getLastName() == null || profile.getLastName().isBlank()) {
            throw new MissingProfileException("User profile is incomplete");
        }
    }

    private Set<UserApplicationStatus> activeStatuses() {
        return EnumSet.of(
                UserApplicationStatus.DRAFT,
                UserApplicationStatus.SUBMITTED,
                UserApplicationStatus.IN_REVIEW
        );
    }

    private List<String> allowedSortProperties() {
        return List.of("createdAt", "lastStatusChangedAt", "status");
    }

    private void validatePageable(Pageable pageable, List<String> allowedProperties) {
        if (pageable.getPageNumber() < 0) {
            throw new InvalidPaginationException("Page index must not be negative");
        }
        if (pageable.getPageSize() > 100) {
            throw new InvalidPaginationException("Page size must be <= 100");
        }
        pageable.getSort().forEach(order -> {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new InvalidPaginationException("Invalid sort property: " + order.getProperty());
            }
        });
    }

    private void validateSort(Sort sort, List<String> allowedProperties) {
        sort.forEach(order -> {
            if (!allowedProperties.contains(order.getProperty())) {
                throw new InvalidPaginationException("Invalid sort property: " + order.getProperty());
            }
        });
    }

    private boolean isValidUserTransition(UserApplicationStatus current, UserApplicationStatus target) {
        return current == UserApplicationStatus.DRAFT && target == UserApplicationStatus.SUBMITTED;
    }

    private boolean isValidAdminTransition(UserApplicationStatus current, UserApplicationStatus target) {
        if (current == UserApplicationStatus.SUBMITTED) {
            return target == UserApplicationStatus.IN_REVIEW;
        }
        if (current == UserApplicationStatus.IN_REVIEW) {
            return target == UserApplicationStatus.APPROVED || target == UserApplicationStatus.REJECTED;
        }
        return false;
    }

    private List<Long> resolveUserIds(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String normalized = "%" + query.toLowerCase(java.util.Locale.ROOT) + "%";
        return userAccountRepository.findIdsBySearch(normalized);
    }
}
