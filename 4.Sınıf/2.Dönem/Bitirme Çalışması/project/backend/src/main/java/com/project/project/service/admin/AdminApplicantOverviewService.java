package com.project.project.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.project.project.dto.admin.ApplicantOverviewResponse;
import com.project.project.entity.UserApplicationStatus;

/**
 * Defines operations for the admin applicant overview dashboard.
 */
public interface AdminApplicantOverviewService {

    Page<ApplicantOverviewResponse> getOverview(
            UserApplicationStatus status,
            String positionKey,
            Boolean hasCv,
            Boolean hasAvailability,
            String query,
            Pageable pageable
    );

    java.util.List<ApplicantOverviewResponse> getOverviewForExport(
            UserApplicationStatus status,
            String positionKey,
            Boolean hasCv,
            Boolean hasAvailability,
            String query,
            org.springframework.data.domain.Sort sort
    );
}
