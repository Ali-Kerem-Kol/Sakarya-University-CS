package com.project.project.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.config.exception.InvalidPaginationException;
import com.project.project.dto.admin.AdminUserSummaryResponse;
import com.project.project.dto.admin.UpdateUserColorRequest;
import com.project.project.dto.user.UserProfileResponse;
import com.project.project.service.admin.AdminUserService;

/**
 * Exposes admin endpoints for user profile management.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminUserSummaryResponse>> listUsers(
            @PageableDefault(sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminUserService.listUsers(normalizePageable(pageable)));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUserProfile(userId));
    }

    @PatchMapping("/{userId}/color")
    public ResponseEntity<AdminUserSummaryResponse> updateUserColor(
            @PathVariable Long userId,
            @RequestBody UpdateUserColorRequest request
    ) {
        return ResponseEntity.ok(adminUserService.updateUserColor(userId, request.color()));
    }

    private Pageable normalizePageable(Pageable pageable) {
        int requestedPage = pageable.getPageNumber();
        int requestedSize = pageable.getPageSize();

        if (requestedPage < 0) {
            throw new InvalidPaginationException("Page index must be >= 0 (0 or 1 can be used as first page)");
        }
        if (requestedSize < 1) {
            throw new InvalidPaginationException("Page size must be >= 1");
        }

        int normalizedPage = requestedPage == 0 ? 0 : requestedPage - 1;
        int normalizedSize = Math.min(requestedSize, MAX_PAGE_SIZE);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "createdAt");
        return PageRequest.of(normalizedPage, normalizedSize, sort);
    }
}
