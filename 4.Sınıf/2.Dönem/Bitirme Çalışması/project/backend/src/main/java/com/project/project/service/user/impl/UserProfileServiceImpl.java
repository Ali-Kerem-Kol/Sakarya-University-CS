package com.project.project.service.user.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.user.UserProfileResponse;
import com.project.project.dto.user.UserProfileUpdateRequest;
import com.project.project.entity.UserAccount;
import com.project.project.entity.Document;
import com.project.project.entity.UserProfile;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.service.user.UserProfileService;

/**
 * Implements profile retrieval and update logic for the authenticated user.
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;

    public UserProfileServiceImpl(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        UserAccount userAccount = getUserAccount(userId);
        return toResponse(userAccount, userAccount.getProfile());
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserAccount userAccount = getUserAccount(userId);
        UserProfile profile = userAccount.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserAccount(userAccount);
        }
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setDateOfBirth(request.dateOfBirth());
        UserProfile savedProfile = userProfileRepository.save(profile);
        return toResponse(userAccount, savedProfile);
    }

    private UserAccount getUserAccount(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserProfileResponse toResponse(UserAccount userAccount, UserProfile profile) {
        Document cv = profile != null ? profile.getCvDocument() : null;
        boolean hasCv = cv != null;
        String cvFileName = hasCv ? cv.getOriginalFileName() : null;
        java.time.Instant cvUploadedAt = hasCv ? cv.getUploadedAt() : null;
        String cvDownloadUrl = hasCv ? "/users/me/documents/" + cv.getId() + "/download" : null;

        if (profile == null) {
            return new UserProfileResponse(
                    userAccount.getId(),
                    userAccount.getEmail(),
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
                userAccount.getEmail(),
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
}
