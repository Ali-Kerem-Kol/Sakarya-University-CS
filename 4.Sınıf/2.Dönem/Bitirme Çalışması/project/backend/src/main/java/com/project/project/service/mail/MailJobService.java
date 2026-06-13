package com.project.project.service.mail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.config.exception.NoRecipientsException;
import com.project.project.config.exception.NotFoundException;
import com.project.project.dto.mail.MailJobCreateRequest;
import com.project.project.dto.mail.MailJobResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.entity.Document;
import com.project.project.entity.MailJob;
import com.project.project.entity.MailJobAttachment;
import com.project.project.entity.MailJobType;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.repository.ApplicationPostingRepository;
import com.project.project.repository.ApplicationSubmissionRepository;
import com.project.project.repository.DocumentRepository;
import com.project.project.repository.MailJobAttachmentRepository;
import com.project.project.repository.MailJobRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.service.storage.DocumentType;
import com.project.project.service.storage.FileStorageResult;
import com.project.project.service.storage.FileStorageService;

/**
 * Creates mail jobs and dispatches them asynchronously via SMTP.
 */
@Service
public class MailJobService {

    private final MailJobRepository mailJobRepository;
    private final MailJobAttachmentRepository mailJobAttachmentRepository;
    private final ApplicationSubmissionRepository applicationSubmissionRepository;
    private final ApplicationPostingRepository postingRepository;
    private final UserAccountRepository userAccountRepository;
    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final MailJobAsyncSender mailJobAsyncSender;
    private final ObjectMapper objectMapper;

    public MailJobService(
            MailJobRepository mailJobRepository,
            MailJobAttachmentRepository mailJobAttachmentRepository,
            ApplicationSubmissionRepository applicationSubmissionRepository,
            ApplicationPostingRepository postingRepository,
            UserAccountRepository userAccountRepository,
            DocumentRepository documentRepository,
            FileStorageService fileStorageService,
            MailJobAsyncSender mailJobAsyncSender,
            ObjectMapper objectMapper
    ) {
        this.mailJobRepository = mailJobRepository;
        this.mailJobAttachmentRepository = mailJobAttachmentRepository;
        this.applicationSubmissionRepository = applicationSubmissionRepository;
        this.postingRepository = postingRepository;
        this.userAccountRepository = userAccountRepository;
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.mailJobAsyncSender = mailJobAsyncSender;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MailJobResponse createCategoryJob(
            Long adminId,
            ApplicationCategory category,
            MailJobCreateRequest request,
            List<MultipartFile> files
    ) {
        UserAccount admin = getAdmin(adminId);
        Set<String> recipients = applicationSubmissionRepository.findRecipientEmailsByCategory(category);
        if (recipients.isEmpty()) {
            throw new NoRecipientsException("No recipients found for category");
        }
        List<Document> attachments = saveMailAttachments(admin, files);
        MailJob saved = createJob(admin, MailJobType.CATEGORY, category, null, request, attachments);
        mailJobAsyncSender.dispatch(saved.getId(), recipients, request.subject(), request.body());
        return toResponse(saved);
    }

    @Transactional
    public MailJobResponse createPostingJob(
            Long adminId,
            Long postingId,
            MailJobCreateRequest request,
            List<MultipartFile> files
    ) {
        if (!postingRepository.existsById(postingId)) {
            throw new NotFoundException("Posting not found");
        }
        UserAccount admin = getAdmin(adminId);
        Set<String> recipients = applicationSubmissionRepository.findRecipientEmailsByPostingId(postingId);
        if (recipients.isEmpty()) {
            throw new NoRecipientsException("No recipients found for posting");
        }
        List<Document> attachments = saveMailAttachments(admin, files);
        MailJob saved = createJob(admin, MailJobType.POSTING, null, postingId, request, attachments);
        mailJobAsyncSender.dispatch(saved.getId(), recipients, request.subject(), request.body());
        return toResponse(saved);
    }

    @Transactional
    public MailJobResponse createAllStudentsJob(
            Long adminId,
            MailJobCreateRequest request,
            List<MultipartFile> files
    ) {
        UserAccount admin = getAdmin(adminId);
        Set<String> recipients = userAccountRepository.findRecipientEmailsForAllStudents(Role.USER);
        if (recipients.isEmpty()) {
            throw new NoRecipientsException("No recipients found for all-students");
        }
        List<Document> attachments = saveMailAttachments(admin, files);
        MailJob saved = createJob(admin, MailJobType.ALL_STUDENTS, null, null, request, attachments);
        mailJobAsyncSender.dispatch(saved.getId(), recipients, request.subject(), request.body());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MailJobResponse get(Long jobId) {
        MailJob job = mailJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Mail job not found"));
        return toResponse(job);
    }

    private UserAccount getAdmin(Long adminId) {
        return userAccountRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private MailJob createJob(
            UserAccount admin,
            MailJobType type,
            ApplicationCategory category,
            Long postingId,
            MailJobCreateRequest request,
            List<Document> attachments
    ) {
        MailJob job = new MailJob();
        job.setType(type);
        job.setPayloadJson(buildPayload(type, category, postingId, request, attachments));
        job.setCreatedByAdmin(admin);
        job.setCreatedAt(Instant.now());
        MailJob saved = mailJobRepository.save(job);

        for (Document attachment : attachments) {
            MailJobAttachment link = new MailJobAttachment();
            link.setMailJob(saved);
            link.setDocument(attachment);
            mailJobAttachmentRepository.save(link);
        }
        return saved;
    }

    private List<Document> saveMailAttachments(UserAccount admin, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        List<Document> docs = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            FileStorageResult result = fileStorageService.storeFile(admin.getId(), DocumentType.MAIL_ATTACHMENT, file);
            Document document = new Document();
            document.setOriginalFileName(result.originalFileName());
            document.setContentType(result.contentType());
            document.setSize(result.size());
            document.setStorageKey(result.storageKey());
            document.setChecksum(result.checksum());
            document.setUploadedBy(admin);
            document.setUploadedAt(Instant.now());
            docs.add(documentRepository.save(document));
        }
        return docs;
    }

    private String buildPayload(
            MailJobType type,
            ApplicationCategory category,
            Long postingId,
            MailJobCreateRequest request,
            List<Document> attachments
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", type.name());
        payload.put("category", category != null ? category.name() : null);
        payload.put("postingId", postingId);
        payload.put("subject", request.subject());
        payload.put("body", request.body());
        payload.put("attachmentDocumentIds", attachments.stream().map(Document::getId).toList());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize mail payload");
        }
    }

    private MailJobResponse toResponse(MailJob job) {
        return new MailJobResponse(
                job.getId(),
                job.getType(),
                job.getStatus(),
                job.getPayloadJson(),
                job.getErrorMessage(),
                job.getCreatedAt()
        );
    }
}
