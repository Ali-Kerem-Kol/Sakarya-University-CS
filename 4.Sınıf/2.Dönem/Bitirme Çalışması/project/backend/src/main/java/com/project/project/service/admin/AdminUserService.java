package com.project.project.service.admin;

import com.project.project.dto.admin.AdminUserSummaryResponse;
import com.project.project.dto.admin.AdminUserCreateResponse;
import com.project.project.dto.admin.CreateAdminUserRequest;
import com.project.project.dto.user.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines admin operations for viewing and managing user profiles.
 */
public interface AdminUserService {

    Page<AdminUserSummaryResponse> listUsers(Pageable pageable);

    UserProfileResponse getUserProfile(Long userId);

    AdminUserSummaryResponse updateUserColor(Long userId, String color);

    AdminUserCreateResponse createAdminUser(CreateAdminUserRequest request);
}
