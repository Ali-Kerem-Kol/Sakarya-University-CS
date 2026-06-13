package com.project.project.service.admin.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.project.config.exception.InvalidPaginationException;
import com.project.project.dto.admin.ApplicantOverviewResponse;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserApplication;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.repository.DocumentRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserApplicationRepository;
import com.project.project.repository.UserAvailabilitySlotRepository;
import com.project.project.service.admin.AdminApplicantOverviewService;

/**
 * Implements the admin applicant overview dashboard aggregation logic.
 */
@Service
public class AdminApplicantOverviewServiceImpl implements AdminApplicantOverviewService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserAccountRepository userAccountRepository;
    private final UserApplicationRepository userApplicationRepository;
    private final DocumentRepository documentRepository;
    private final UserAvailabilitySlotRepository userAvailabilitySlotRepository;

    public AdminApplicantOverviewServiceImpl(
            UserAccountRepository userAccountRepository,
            UserApplicationRepository userApplicationRepository,
            DocumentRepository documentRepository,
            UserAvailabilitySlotRepository userAvailabilitySlotRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userApplicationRepository = userApplicationRepository;
        this.documentRepository = documentRepository;
        this.userAvailabilitySlotRepository = userAvailabilitySlotRepository;
    }

    @Override
    public Page<ApplicantOverviewResponse> getOverview(
            UserApplicationStatus status,
            String positionKey,
            Boolean hasCv,
            Boolean hasAvailability,
            String query,
            Pageable pageable
    ) {
        validatePageable(pageable, allowedSortProperties());
        List<ApplicantOverviewResponse> responses = buildOverviewResponses(
                status,
                positionKey,
                hasCv,
                hasAvailability,
                query
        );
        responses.sort(buildComparator(pageable.getSort()));
        return paginate(responses, pageable);
    }

    @Override
    public List<ApplicantOverviewResponse> getOverviewForExport(
            UserApplicationStatus status,
            String positionKey,
            Boolean hasCv,
            Boolean hasAvailability,
            String query,
            Sort sort
    ) {
        validateSort(sort, allowedSortProperties());
        List<ApplicantOverviewResponse> responses = buildOverviewResponses(
                status,
                positionKey,
                hasCv,
                hasAvailability,
                query
        );
        responses.sort(buildComparator(sort));
        return responses;
    }

    private List<ApplicantOverviewResponse> buildOverviewResponses(
            UserApplicationStatus status,
            String positionKey,
            Boolean hasCv,
            Boolean hasAvailability,
            String query
    ) {
        List<Long> userIds = resolveUserIds(query);
        if (userIds.isEmpty()) {
            return List.of();
        }
        Map<Long, UserApplication> latestApplications = loadLatestApplications(userIds);
        if (status != null || (positionKey != null && !positionKey.isBlank())) {
            userIds = filterByLatestApplication(userIds, latestApplications, status, positionKey);
        }
        if (userIds.isEmpty()) {
            return List.of();
        }
        Set<Long> cvUserIds = documentRepository.findCvUserIdsWithDocuments(userIds);
        Map<Long, Integer> availabilityCounts = loadAvailabilityCounts(userIds);
        if (hasCv != null) {
            userIds = filterByCv(userIds, cvUserIds, hasCv);
        }
        if (hasAvailability != null) {
            userIds = filterByAvailability(userIds, availabilityCounts, hasAvailability);
        }
        if (userIds.isEmpty()) {
            return List.of();
        }
        Map<Long, UserAccount> accounts = loadAccounts(userIds);
        List<ApplicantOverviewResponse> responses = new ArrayList<>();
        for (Long userId : userIds) {
            UserAccount account = accounts.get(userId);
            if (account == null) {
                continue;
            }
            UserApplication latest = latestApplications.get(userId);
            responses.add(new ApplicantOverviewResponse(
                    account.getId(),
                    account.getEmail(),
                    account.getProfile() != null ? account.getProfile().getFirstName() : null,
                    account.getProfile() != null ? account.getProfile().getLastName() : null,
                    latest != null ? latest.getId() : null,
                    latest != null ? latest.getPositionKey() : null,
                    latest != null ? latest.getStatus() : null,
                    latest != null ? latest.getLastStatusChangedAt() : null,
                    cvUserIds.contains(userId),
                    availabilityCounts.getOrDefault(userId, 0)
            ));
        }
        return responses;
    }

    private List<Long> resolveUserIds(String query) {
        if (query == null || query.isBlank()) {
            return userAccountRepository.findAllIds();
        }
        String normalized = "%" + query.toLowerCase(Locale.ROOT) + "%";
        return userAccountRepository.findIdsBySearch(normalized);
    }

    private Map<Long, UserApplication> loadLatestApplications(List<Long> userIds) {
        List<UserApplication> applications =
                userApplicationRepository.findByUserAccountIdInOrderByCreatedAtDesc(userIds);
        Map<Long, UserApplication> latest = new HashMap<>();
        for (UserApplication application : applications) {
            Long userId = application.getUserAccount().getId();
            latest.putIfAbsent(userId, application);
        }
        return latest;
    }

    private List<Long> filterByLatestApplication(
            List<Long> userIds,
            Map<Long, UserApplication> latestApplications,
            UserApplicationStatus status,
            String positionKey
    ) {
        List<Long> filtered = new ArrayList<>();
        for (Long userId : userIds) {
            UserApplication application = latestApplications.get(userId);
            if (application == null) {
                continue;
            }
            if (status != null && application.getStatus() != status) {
                continue;
            }
            if (positionKey != null && !positionKey.isBlank()
                    && !application.getPositionKey().equals(positionKey)) {
                continue;
            }
            filtered.add(userId);
        }
        return filtered;
    }

    private Map<Long, Integer> loadAvailabilityCounts(List<Long> userIds) {
        Map<Long, Integer> counts = new HashMap<>();
        for (Object[] row : userAvailabilitySlotRepository.countByUserIds(userIds)) {
            Long userId = (Long) row[0];
            Long count = (Long) row[1];
            counts.put(userId, count.intValue());
        }
        return counts;
    }

    private List<Long> filterByCv(List<Long> userIds, Set<Long> cvUserIds, boolean hasCv) {
        return userIds.stream()
                .filter(id -> hasCv == cvUserIds.contains(id))
                .collect(Collectors.toList());
    }

    private List<Long> filterByAvailability(
            List<Long> userIds,
            Map<Long, Integer> availabilityCounts,
            boolean hasAvailability
    ) {
        return userIds.stream()
                .filter(id -> hasAvailability == (availabilityCounts.getOrDefault(id, 0) > 0))
                .collect(Collectors.toList());
    }

    private Map<Long, UserAccount> loadAccounts(List<Long> userIds) {
        List<UserAccount> accounts = userAccountRepository.findByIdInWithProfile(userIds);
        Map<Long, UserAccount> map = new HashMap<>();
        for (UserAccount account : accounts) {
            map.put(account.getId(), account);
        }
        return map;
    }

    private Page<ApplicantOverviewResponse> paginate(
            List<ApplicantOverviewResponse> responses,
            Pageable pageable
    ) {
        int total = responses.size();
        int fromIndex = pageable.getPageNumber() * pageable.getPageSize();
        if (fromIndex >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), total);
        return new PageImpl<>(responses.subList(fromIndex, toIndex), pageable, total);
    }

    private Comparator<ApplicantOverviewResponse> buildComparator(Sort sort) {
        List<Sort.Order> orders = new ArrayList<>();
        sort.forEach(orders::add);
        if (orders.isEmpty()) {
            orders = List.of(new Sort.Order(Sort.Direction.DESC, "latestLastStatusChangedAt"));
        }
        Comparator<ApplicantOverviewResponse> comparator = comparatorFor(orders.get(0).getProperty());
        if (orders.get(0).getDirection().isDescending()) {
            comparator = comparator.reversed();
        }
        for (int i = 1; i < orders.size(); i++) {
            Sort.Order order = orders.get(i);
            Comparator<ApplicantOverviewResponse> next = comparatorFor(order.getProperty());
            if (order.getDirection().isDescending()) {
                next = next.reversed();
            }
            comparator = comparator.thenComparing(next);
        }
        return comparator;
    }

    private Comparator<ApplicantOverviewResponse> comparatorFor(String property) {
        return switch (property) {
            case "email" -> Comparator.comparing(
                    ApplicantOverviewResponse::email,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case "userId" -> Comparator.comparing(
                    ApplicantOverviewResponse::userId,
                    Comparator.nullsLast(Long::compareTo)
            );
            case "latestLastStatusChangedAt" -> Comparator.comparing(
                    ApplicantOverviewResponse::latestLastStatusChangedAt,
                    Comparator.nullsLast(Instant::compareTo)
            );
            case "latestStatus" -> Comparator.comparing(
                    response -> response.latestStatus() != null ? response.latestStatus().name() : null,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
            case "availabilitySlotCount" -> Comparator.comparingInt(ApplicantOverviewResponse::availabilitySlotCount);
            default -> throw new InvalidPaginationException("Invalid sort property: " + property);
        };
    }

    private List<String> allowedSortProperties() {
        return List.of(
                "email",
                "userId",
                "latestLastStatusChangedAt",
                "latestStatus",
                "availabilitySlotCount"
        );
    }

    private void validatePageable(Pageable pageable, List<String> allowedProperties) {
        if (pageable.getPageNumber() < 0) {
            throw new InvalidPaginationException("Page index must not be negative");
        }
        if (pageable.getPageSize() < 1 || pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("Page size must be between 1 and 100");
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
}
