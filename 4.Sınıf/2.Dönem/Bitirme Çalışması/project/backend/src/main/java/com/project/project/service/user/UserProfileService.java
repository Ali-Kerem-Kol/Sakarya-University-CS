package com.project.project.service.user;

import com.project.project.dto.user.UserProfileResponse;
import com.project.project.dto.user.UserProfileUpdateRequest;

/**
 * Defines operations for managing the authenticated user's profile.
 */
public interface UserProfileService {

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request);
}
