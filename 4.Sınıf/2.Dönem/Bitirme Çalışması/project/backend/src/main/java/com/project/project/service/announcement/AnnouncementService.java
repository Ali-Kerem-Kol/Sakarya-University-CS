package com.project.project.service.announcement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.announcement.AnnouncementRequest;
import com.project.project.dto.announcement.AnnouncementResponse;
import com.project.project.entity.Announcement;
import com.project.project.entity.UserAccount;
import com.project.project.repository.AnnouncementRepository;
import com.project.project.repository.UserAccountRepository;

/**
 * CRUD service for announcements.
 */
@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserAccountRepository userAccountRepository;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.announcementRepository = announcementRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listPublic() {
        return listAdmin();
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAdmin() {
        List<AnnouncementResponse> responses = new ArrayList<>();
        for (Announcement announcement : announcementRepository.findAllByOrderByCreatedAtDesc()) {
            responses.add(toResponse(announcement));
        }
        return responses;
    }

    @Transactional
    public AnnouncementResponse create(Long adminId, AnnouncementRequest request) {
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));
        Announcement announcement = new Announcement();
        announcement.setTitle(request.title());
        announcement.setContent(request.content());
        announcement.setCreatedByAdmin(admin);
        return toResponse(announcementRepository.save(announcement));
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Announcement not found"));
        announcement.setTitle(request.title());
        announcement.setContent(request.content());
        return toResponse(announcementRepository.save(announcement));
    }

    @Transactional
    public void delete(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new NotFoundException("Announcement not found");
        }
        announcementRepository.deleteById(id);
    }

    private AnnouncementResponse toResponse(Announcement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getCreatedAt()
        );
    }
}
