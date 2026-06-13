package com.project.project.service.mail;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.project.entity.MailJob;
import com.project.project.entity.MailJobAttachment;
import com.project.project.entity.MailJobStatus;
import com.project.project.repository.MailJobAttachmentRepository;
import com.project.project.repository.MailJobRepository;
import com.project.project.service.storage.FileStorageService;

import jakarta.mail.internet.MimeMessage;

/**
 * Async SMTP dispatch worker for mail jobs.
 */
@Service
public class MailJobAsyncSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailJobAsyncSender.class);

    private final MailJobRepository mailJobRepository;
    private final MailJobAttachmentRepository mailJobAttachmentRepository;
    private final FileStorageService fileStorageService;
    private final JavaMailSender javaMailSender;
    private final String from;

    public MailJobAsyncSender(
            MailJobRepository mailJobRepository,
            MailJobAttachmentRepository mailJobAttachmentRepository,
            FileStorageService fileStorageService,
            JavaMailSender javaMailSender,
            @Value("${app.mail.from:noreply@32bit.com.tr}") String from
    ) {
        this.mailJobRepository = mailJobRepository;
        this.mailJobAttachmentRepository = mailJobAttachmentRepository;
        this.fileStorageService = fileStorageService;
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    @Async("mailTaskExecutor")
    @Transactional
    public void dispatch(Long jobId, Set<String> recipients, String subject, String body) {
        MailJob job = mailJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }
        job.setStatus(MailJobStatus.PROCESSING);
        job.setErrorMessage(null);
        mailJobRepository.save(job);

        try {
            List<MailJobAttachment> attachments = mailJobAttachmentRepository.findByMailJobId(jobId);
            for (String recipient : recipients) {
                sendSingle(recipient, subject, body, attachments, jobId);
            }
            job.setStatus(MailJobStatus.DONE);
            mailJobRepository.save(job);
        } catch (Exception ex) {
            job.setStatus(MailJobStatus.FAILED);
            job.setErrorMessage(ex.getMessage());
            mailJobRepository.save(job);
            LOGGER.error("Mail job failed: jobId={}", jobId, ex);
        }
    }

    private void sendSingle(
            String recipient,
            String subject,
            String body,
            List<MailJobAttachment> attachments,
            Long jobId
    ) throws Exception {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(body, false);

        for (MailJobAttachment attachment : attachments) {
            Resource resource = fileStorageService.loadFileAsResource(attachment.getDocument().getStorageKey());
            helper.addAttachment(attachment.getDocument().getOriginalFileName(), resource);
        }

        javaMailSender.send(mimeMessage);
        LOGGER.info("Mail recipient sent: jobId={}, recipient={}, result=SUCCESS", jobId, recipient);
    }
}
