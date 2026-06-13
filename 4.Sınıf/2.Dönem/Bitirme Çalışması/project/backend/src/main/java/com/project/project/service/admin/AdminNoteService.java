package com.project.project.service.admin;

import java.util.List;

import com.project.project.dto.admin.AdminNoteResponse;
import com.project.project.dto.admin.CreateAdminNoteRequest;

/**
 * Defines operations for admin-authored notes on user applications.
 */
public interface AdminNoteService {

    AdminNoteResponse addNote(Long applicationId, String adminEmail, CreateAdminNoteRequest request);

    List<AdminNoteResponse> listNotes(Long applicationId);
}
