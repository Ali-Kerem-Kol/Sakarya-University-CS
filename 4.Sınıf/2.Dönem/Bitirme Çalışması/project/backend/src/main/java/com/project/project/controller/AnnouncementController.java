package com.project.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.announcement.AnnouncementRequest;
import com.project.project.dto.announcement.AnnouncementResponse;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.announcement.AnnouncementService;

/**
 * Announcement endpoints.
 */
@RestController
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping("/api/v1/public/announcements")
    public ResponseEntity<List<AnnouncementResponse>> listPublic() {
        return ResponseEntity.ok(announcementService.listPublic());
    }

    @GetMapping("/api/v1/admin/announcements")
    public ResponseEntity<List<AnnouncementResponse>> listAdmin() {
        return ResponseEntity.ok(announcementService.listAdmin());
    }

    @PostMapping("/api/v1/admin/announcements")
    public ResponseEntity<AnnouncementResponse> create(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.status(201)
                .body(announcementService.create(principal.getUserAccount().getId(), request));
    }

    @PutMapping("/api/v1/admin/announcements/{id}")
    public ResponseEntity<AnnouncementResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request
    ) {
        return ResponseEntity.ok(announcementService.update(id, request));
    }

    @DeleteMapping("/api/v1/admin/announcements/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
