package com.project.project.service.admin.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.project.dto.admin.ApplicantOverviewResponse;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.service.admin.AdminApplicantOverviewService;
import com.project.project.service.export.CsvExportUtil;

/**
 * Streams applicant overview exports for admin usage.
 */
@Service
public class AdminApplicantOverviewExportService {

    private static final DateTimeFormatter INSTANT_FORMAT = DateTimeFormatter.ISO_INSTANT;

    private final AdminApplicantOverviewService overviewService;

    public AdminApplicantOverviewExportService(AdminApplicantOverviewService overviewService) {
        this.overviewService = overviewService;
    }

    public void writeOverviewCsv(
            UserApplicationStatus status,
            String positionKey,
            Boolean hasCv,
            Boolean hasAvailability,
            String query,
            Sort sort,
            Writer writer
    ) throws IOException {
        CsvExportUtil.writeRow(writer, List.of(
                "userId",
                "email",
                "firstName",
                "lastName",
                "latestApplicationId",
                "latestPositionKey",
                "latestStatus",
                "latestLastStatusChangedAt",
                "hasCv",
                "availabilitySlotCount"
        ));
        List<ApplicantOverviewResponse> responses = overviewService.getOverviewForExport(
                status,
                positionKey,
                hasCv,
                hasAvailability,
                query,
                sort
        );
        for (ApplicantOverviewResponse response : responses) {
            CsvExportUtil.writeRow(writer, List.of(
                    String.valueOf(response.userId()),
                    response.email(),
                    response.firstName(),
                    response.lastName(),
                    response.latestApplicationId() != null ? String.valueOf(response.latestApplicationId()) : "",
                    response.latestPositionKey(),
                    response.latestStatus() != null ? response.latestStatus().name() : "",
                    formatInstant(response.latestLastStatusChangedAt()),
                    String.valueOf(response.hasCv()),
                    String.valueOf(response.availabilitySlotCount())
            ));
        }
        writer.flush();
    }

    private String formatInstant(java.time.Instant instant) {
        return instant != null ? INSTANT_FORMAT.format(instant) : "";
    }

    public Writer buildWriter(java.io.OutputStream outputStream) {
        return new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
    }
}
