package com.project.project.service.storage;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.project.config.AppStorageProperties;
import com.project.project.config.exception.BadRequestException;
import com.project.project.config.exception.FileTooLargeException;
import com.project.project.config.exception.InvalidFileTypeException;
import com.project.project.config.exception.NotFoundException;

/**
 * Stores and loads files from the local filesystem with validation safeguards.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Map<String, String> CONTENT_TYPE_EXTENSION = Map.of(
            "application/pdf", "pdf",
            "image/png", "png",
            "image/jpeg", "jpg",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx",
            "application/vnd.ms-excel", "xls"
    );

    private final AppStorageProperties properties;
    private final Path rootPath;

    public FileStorageServiceImpl(AppStorageProperties properties) {
        this.properties = properties;
        this.rootPath = Paths.get(properties.getRoot()).toAbsolutePath().normalize();
    }

    @Override
    public void validateFile(DocumentType documentType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new FileTooLargeException("File exceeds max allowed size");
        }
        String extension = resolveExtension(file);
        String filenameExtension = resolveFilenameExtension(file);
        if (documentType == DocumentType.CV) {
            if (!"pdf".equals(extension) || (filenameExtension != null && !"pdf".equals(filenameExtension))) {
                throw new InvalidFileTypeException("CV must be a PDF");
            }
            return;
        }
        if (documentType == DocumentType.POSTING_ATTACHMENT) {
            if (!"pdf".equals(extension) || (filenameExtension != null && !"pdf".equals(filenameExtension))) {
                throw new InvalidFileTypeException("Attachment must be a PDF");
            }
            return;
        }
        if (documentType == DocumentType.TASK_ATTACHMENT || documentType == DocumentType.TASK_SUBMISSION) {
            if (!"pdf".equals(extension) || (filenameExtension != null && !"pdf".equals(filenameExtension))) {
                throw new InvalidFileTypeException("Task file must be a PDF");
            }
            return;
        }
        if (documentType == DocumentType.MAIL_ATTACHMENT) {
            if (!isAllowedMailAttachmentExtension(extension)
                    || (filenameExtension != null && !isAllowedMailAttachmentExtension(filenameExtension))) {
                throw new InvalidFileTypeException("Mail attachment must be pdf/image/spreadsheet");
            }
            return;
        }
        throw new InvalidFileTypeException("Unsupported document type");
    }

    @Override
    public FileStorageResult storeFile(Long userId, DocumentType documentType, MultipartFile file) {
        validateFile(documentType, file);
        String contentType = normalizeContentType(file.getContentType());
        String extension = resolveExtension(file);
        String storedFileName = UUID.randomUUID() + "." + extension;
        String originalFileName = sanitizeOriginalFileName(file.getOriginalFilename());
        String relativePath = buildRelativePath(userId, documentType, storedFileName);
        Path targetPath = rootPath.resolve(relativePath).normalize();
        ensureWithinRoot(targetPath);
        MessageDigest messageDigest = buildSha256Digest();
        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = file.getInputStream();
                 DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest)) {
                Files.copy(digestInputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new BadRequestException("Failed to store file");
        }
        return new FileStorageResult(
                originalFileName,
                resolveContentType(contentType, extension),
                file.getSize(),
                relativePath,
                bytesToHex(messageDigest.digest())
        );
    }

    @Override
    public Resource loadFileAsResource(String storagePath) {
        Path resolvedPath = rootPath.resolve(storagePath).normalize();
        ensureWithinRoot(resolvedPath);
        try {
            Resource resource = new UrlResource(resolvedPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("File not found");
            }
            return resource;
        } catch (IOException ex) {
            throw new NotFoundException("File not found");
        }
    }

    private String buildRelativePath(Long userId, DocumentType documentType, String storedFileName) {
        String folder = switch (documentType) {
            case CV -> "cv";
            case POSTING_ATTACHMENT -> "posting-attachments";
            case MAIL_ATTACHMENT -> "mail-attachments";
            case TASK_ATTACHMENT -> "task-attachments";
            case TASK_SUBMISSION -> "task-submissions";
        };
        return "users/" + userId + "/" + folder + "/" + storedFileName;
    }

    private void ensureWithinRoot(Path targetPath) {
        if (!targetPath.startsWith(rootPath)) {
            throw new BadRequestException("Invalid storage path");
        }
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        String normalized = contentType.toLowerCase(Locale.ROOT);
        int separatorIndex = normalized.indexOf(';');
        if (separatorIndex > 0) {
            return normalized.substring(0, separatorIndex).trim();
        }
        return normalized.trim();
    }

    private String sanitizeOriginalFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            return "unknown";
        }
        return originalFileName.replace("\\", "/").replaceAll(".*/", "");
    }

    private String resolveExtension(MultipartFile file) {
        String contentType = normalizeContentType(file.getContentType());
        String extension = CONTENT_TYPE_EXTENSION.get(contentType);
        if (StringUtils.hasText(extension)) {
            return extension;
        }
        String fileExtension = resolveFilenameExtension(file);
        if (fileExtension != null) {
            return fileExtension;
        }
        throw new InvalidFileTypeException("Unsupported file type");
    }

    private String resolveFilenameExtension(MultipartFile file) {
        String fileName = sanitizeOriginalFileName(file.getOriginalFilename());
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > -1 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private boolean isAllowedMailAttachmentExtension(String extension) {
        return List.of("pdf", "png", "jpg", "jpeg", "xls", "xlsx").contains(extension);
    }

    private String resolveContentType(String contentType, String extension) {
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> "application/octet-stream";
        };
    }

    private MessageDigest buildSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new BadRequestException("Checksum algorithm unavailable");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
