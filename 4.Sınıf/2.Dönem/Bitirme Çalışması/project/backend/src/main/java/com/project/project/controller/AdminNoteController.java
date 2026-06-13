package com.project.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.admin.AdminNoteResponse;
import com.project.project.dto.admin.CreateAdminNoteRequest;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.admin.AdminNoteService;

/**
 * Exposes admin endpoints for managing notes on user applications.
 */
@RestController
@RequestMapping("/api/v1/admin/applications")
public class AdminNoteController {

    private final AdminNoteService adminNoteService;

    public AdminNoteController(AdminNoteService adminNoteService) {
        this.adminNoteService = adminNoteService;
    }

    @PostMapping("/{applicationId}/notes")
    public ResponseEntity<AdminNoteResponse> addNote(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateAdminNoteRequest request
    ) {
        return ResponseEntity.status(201).body(
                adminNoteService.addNote(applicationId, principal.getUsername(), request)
        );
    }

    @GetMapping("/{applicationId}/notes")
    public ResponseEntity<List<AdminNoteResponse>> listNotes(@PathVariable Long applicationId) {
        return ResponseEntity.ok(adminNoteService.listNotes(applicationId));
    }
}
