package com.project.project.service.admin.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.project.dto.user.ApplicationResponse;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserApplicationStatus;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.export.CsvExportUtil;
import com.project.project.service.user.UserApplicationService;

/**
 * Streams application exports for admin usage.
 */
@Service
public class AdminApplicationExportService {

    private static final DateTimeFormatter INSTANT_FORMAT = DateTimeFormatter.ISO_INSTANT;

    private final UserApplicationService userApplicationService;
    private final UserAccountRepository userAccountRepository;

    public AdminApplicationExportService(
            UserApplicationService userApplicationService,
            UserAccountRepository userAccountRepository
    ) {
        this.userApplicationService = userApplicationService;
        this.userAccountRepository = userAccountRepository;
    }

    public void writeApplicationsCsv(
            UserApplicationStatus status,
            String positionKey,
            String query,
            Sort sort,
            Writer writer
    ) throws IOException {
        CsvExportUtil.writeRow(writer, List.of(
                "applicationId",
                "userId",
                "email",
                "positionKey",
                "status",
                "createdAt",
                "lastStatusChangedAt"
        ));
        List<ApplicationResponse> applications =
                userApplicationService.adminExportApplications(status, positionKey, query, sort);
        Map<Long, UserAccount> accounts = loadAccounts(applications);
        for (ApplicationResponse application : applications) {
            UserAccount account = accounts.get(application.userId());
            String email = account != null ? account.getEmail() : "";
            CsvExportUtil.writeRow(writer, List.of(
                    String.valueOf(application.id()),
                    String.valueOf(application.userId()),
                    email,
                    application.positionKey(),
                    application.status() != null ? application.status().name() : "",
                    formatInstant(application.createdAt()),
                    formatInstant(application.lastStatusChangedAt())
            ));
        }
        writer.flush();
    }

    private Map<Long, UserAccount> loadAccounts(List<ApplicationResponse> applications) {
        List<Long> userIds = applications.stream()
                .map(ApplicationResponse::userId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return java.util.Map.of();
        }
        List<UserAccount> accounts = userAccountRepository.findByIdIn(userIds);
        return accounts.stream().collect(java.util.stream.Collectors.toMap(UserAccount::getId, account -> account));
    }

    private String formatInstant(java.time.Instant instant) {
        return instant != null ? INSTANT_FORMAT.format(instant) : "";
    }

    public Writer buildWriter(java.io.OutputStream outputStream) {
        return new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
    }
}
