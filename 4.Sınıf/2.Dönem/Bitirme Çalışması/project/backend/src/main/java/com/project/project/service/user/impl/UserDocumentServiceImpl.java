package com.project.project.service.user.impl;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.user.DocumentResponse;
import com.project.project.dto.user.UserDocumentsResponse;
import com.project.project.entity.Document;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserProfile;
import com.project.project.repository.DocumentRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.service.storage.DocumentType;
import com.project.project.service.storage.FileStorageResult;
import com.project.project.service.storage.FileStorageService;
import com.project.project.service.user.UserDocumentService;

/**
 * Implements user document upload logic with a unified Document entity.
 */
@Service
public class UserDocumentServiceImpl implements UserDocumentService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    public UserDocumentServiceImpl(
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            DocumentRepository documentRepository,
            FileStorageService fileStorageService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public DocumentResponse uploadCv(Long userId, MultipartFile file) {
        UserAccount userAccount = getUserAccount(userId);
        UserProfile profile = ensureProfile(userAccount);
        FileStorageResult storageResult = fileStorageService.storeFile(userId, DocumentType.CV, file);

        Document document = buildDocument(storageResult, userAccount);
        Document saved = documentRepository.save(document);

        // Keep historical documents and only rotate the relation.
        profile.setCvDocument(saved);
        userProfileRepository.save(profile);

        return toResponse(saved, "CV");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDocumentsResponse listDocuments(Long userId) {
        return listDocumentsForUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownload downloadDocument(Long userId, Long documentId) {
        Document document = documentRepository.findByIdAndUploadedById(documentId, userId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        Resource resource = fileStorageService.loadFileAsResource(document.getStorageKey());
        return new DocumentDownload(resource, document.getContentType(), document.getOriginalFileName());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDocumentsResponse listDocumentsForUser(Long userId) {
        List<DocumentResponse> cvDocuments = List.of();
        UserProfile profile = userProfileRepository.findByUserAccountId(userId).orElse(null);
        if (profile != null && profile.getCvDocument() != null) {
            cvDocuments = List.of(toResponse(profile.getCvDocument(), "CV"));
        } else {
            // Fallback for legacy/inconsistent rows where relation is missing but CV exists in documents.
            cvDocuments = documentRepository.findByUploadedByIdOrderByUploadedAtDesc(userId).stream()
                    .filter(document -> document.getStorageKey() != null
                            && document.getStorageKey().toLowerCase(java.util.Locale.ROOT).contains("/cv/"))
                    .findFirst()
                    .map(document -> List.of(toResponse(document, "CV")))
                    .orElse(List.of());
        }
        return new UserDocumentsResponse(cvDocuments);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownload downloadDocumentForUser(Long userId, Long documentId) {
        Document document = documentRepository.findByIdAndUploadedById(documentId, userId)
                .orElseGet(() -> {
                    UserProfile profile = userProfileRepository.findByUserAccountId(userId).orElse(null);
                    if (profile != null && profile.getCvDocument() != null
                            && profile.getCvDocument().getId().equals(documentId)) {
                        return profile.getCvDocument();
                    }
                    throw new NotFoundException("Document not found");
                });
        Resource resource = fileStorageService.loadFileAsResource(document.getStorageKey());
        return new DocumentDownload(resource, document.getContentType(), document.getOriginalFileName());
    }

    private UserAccount getUserAccount(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserProfile ensureProfile(UserAccount userAccount) {
        UserProfile profile = userAccount.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserAccount(userAccount);
        }
        return userProfileRepository.save(profile);
    }

    private Document buildDocument(FileStorageResult storageResult, UserAccount uploadedBy) {
        Document document = new Document();
        document.setOriginalFileName(storageResult.originalFileName());
        document.setContentType(storageResult.contentType());
        document.setSize(storageResult.size());
        document.setStorageKey(storageResult.storageKey());
        document.setChecksum(storageResult.checksum());
        document.setUploadedBy(uploadedBy);
        document.setUploadedAt(java.time.Instant.now());
        return document;
    }

    private DocumentResponse toResponse(Document document, String type) {
        return new DocumentResponse(
                document.getId(),
                type,
                document.getOriginalFileName(),
                document.getContentType(),
                document.getSize(),
                document.getUploadedAt()
        );
    }
}
