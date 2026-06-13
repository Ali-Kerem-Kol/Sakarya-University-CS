package com.project.project.service.user;

import com.project.project.dto.user.ApplicationListResponse;
import com.project.project.dto.user.ApplicationResponse;
import com.project.project.dto.user.CreateApplicationRequest;
import com.project.project.entity.UserApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Defines operations for user-created applications.
 */
public interface UserApplicationService {

    ApplicationResponse createForCurrentUser(Long userId, CreateApplicationRequest request);

    ApplicationListResponse listForCurrentUser(Long userId);

    ApplicationResponse getForCurrentUser(Long userId, Long applicationId);

    ApplicationResponse submitApplication(Long userId, Long applicationId);

    Page<ApplicationResponse> adminList(UserApplicationStatus status, String positionKey, Pageable pageable);

    ApplicationResponse adminGetById(Long applicationId);

    ApplicationResponse adminUpdateStatus(Long applicationId, UserApplicationStatus status);

    java.util.List<ApplicationResponse> adminExportApplications(
            UserApplicationStatus status,
            String positionKey,
            String query,
            org.springframework.data.domain.Sort sort
    );
}
