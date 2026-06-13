package com.project.project.service.admin.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;

import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.admin.AdminNoteResponse;
import com.project.project.dto.admin.CreateAdminNoteRequest;
import com.project.project.entity.AdminNote;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserApplication;
import com.project.project.repository.AdminNoteRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserApplicationRepository;
import com.project.project.service.admin.AdminNoteService;

/**
 * Implements admin note management for user applications.
 */
@Service
public class AdminNoteServiceImpl implements AdminNoteService {

    private final AdminNoteRepository adminNoteRepository;
    private final UserApplicationRepository userApplicationRepository;
    private final UserAccountRepository userAccountRepository;

    public AdminNoteServiceImpl(
            AdminNoteRepository adminNoteRepository,
            UserApplicationRepository userApplicationRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.adminNoteRepository = adminNoteRepository;
        this.userApplicationRepository = userApplicationRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional
    public AdminNoteResponse addNote(Long applicationId, String adminEmail, CreateAdminNoteRequest request) {
        UserAccount adminAccount = userAccountRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
        if (adminAccount.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can add notes");
        }
        UserApplication application = userApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        AdminNote note = new AdminNote();
        note.setApplication(application);
        note.setCreatedBy(adminAccount);
        note.setNoteText(request.noteText());
        note.setCreatedByAdminEmail(adminAccount.getEmail());
        AdminNote saved = adminNoteRepository.save(note);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminNoteResponse> listNotes(Long applicationId) {
        List<AdminNoteResponse> responses = new ArrayList<>();
        for (AdminNote note : adminNoteRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)) {
            responses.add(toResponse(note));
        }
        return responses;
    }

    private AdminNoteResponse toResponse(AdminNote note) {
        return new AdminNoteResponse(
                note.getId(),
                note.getApplication().getId(),
                note.getNoteText(),
                note.getCreatedAt(),
                note.getCreatedByAdminEmail()
        );
    }
}
