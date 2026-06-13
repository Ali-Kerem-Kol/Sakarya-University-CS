package com.project.project.service.auth.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.project.project.config.exception.BadRequestException;
import com.project.project.config.exception.ConflictException;
import com.project.project.config.exception.CvRequiredException;
import com.project.project.config.exception.InvalidEmailDomainException;
import com.project.project.config.exception.UnauthorizedException;
import com.project.project.dto.auth.ForgotPasswordRequest;
import com.project.project.dto.auth.LoginRequest;
import com.project.project.dto.auth.LoginResponse;
import com.project.project.dto.auth.MessageResponse;
import com.project.project.dto.auth.RegisterRequest;
import com.project.project.dto.auth.RegisterResponse;
import com.project.project.dto.auth.ResetPasswordRequest;
import com.project.project.entity.Document;
import com.project.project.entity.EmailVerificationToken;
import com.project.project.entity.PasswordResetToken;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserProfile;
import com.project.project.repository.DocumentRepository;
import com.project.project.repository.EmailVerificationTokenRepository;
import com.project.project.repository.PasswordResetTokenRepository;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;
import com.project.project.security.JwtProperties;
import com.project.project.security.JwtService;
import com.project.project.service.auth.AuthMailStubService;
import com.project.project.service.auth.AuthService;
import com.project.project.service.auth.EmailDomainPolicy;
import com.project.project.service.storage.DocumentType;
import com.project.project.service.storage.FileStorageResult;
import com.project.project.service.storage.FileStorageService;

/**
 * Implements authentication and registration workflows.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final DocumentRepository documentRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMailStubService authMailStubService;
    private final FileStorageService fileStorageService;
    private final EmailDomainPolicy emailDomainPolicy;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            UserAccountRepository userAccountRepository,
            UserProfileRepository userProfileRepository,
            DocumentRepository documentRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthMailStubService authMailStubService,
            FileStorageService fileStorageService,
            EmailDomainPolicy emailDomainPolicy
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.documentRepository = documentRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authMailStubService = authMailStubService;
        this.fileStorageService = fileStorageService;
        this.emailDomainPolicy = emailDomainPolicy;
    }

    @Override
    public LoginResponse authenticate(LoginRequest request) {
        String normalizedEmail = emailDomainPolicy.normalize(request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );
        UserAccount userAccount = ((com.project.project.security.UserAccountPrincipal) authentication.getPrincipal())
                .getUserAccount();
        try {
            emailDomainPolicy.assertForRole(emailDomainPolicy.normalize(userAccount.getEmail()), userAccount.getRole());
        } catch (InvalidEmailDomainException ex) {
            throw new UnauthorizedException(ex.getMessage());
        }
        String token = jwtService.generateAccessToken(userAccount);
        Instant expiresAt = Instant.now().plus(jwtProperties.getAccessTokenMinutes(), ChronoUnit.MINUTES);
        return new LoginResponse(
                token,
                "Bearer",
                expiresAt,
                userAccount.getId(),
                userAccount.getEmail(),
                userAccount.getRole().name()
        );
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request, MultipartFile cvFile) {
        if (cvFile == null || cvFile.isEmpty()) {
            throw new CvRequiredException("CV file is required");
        }
        String normalizedEmail = emailDomainPolicy.normalize(request.email());
        emailDomainPolicy.assertForRole(normalizedEmail, Role.USER);
        userAccountRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            throw new ConflictException("Email already registered");
        });

        UserAccount account = new UserAccount();
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(Role.USER);
        account.setEnabled(false);
        account.setEmailVerified(false);
        UserAccount saved = userAccountRepository.save(account);

        FileStorageResult cvStorage = fileStorageService.storeFile(saved.getId(), DocumentType.CV, cvFile);
        Document cvDocument = new Document();
        cvDocument.setOriginalFileName(cvStorage.originalFileName());
        cvDocument.setContentType(cvStorage.contentType());
        cvDocument.setSize(cvStorage.size());
        cvDocument.setStorageKey(cvStorage.storageKey());
        cvDocument.setChecksum(cvStorage.checksum());
        cvDocument.setUploadedBy(saved);
        cvDocument.setUploadedAt(Instant.now());
        Document savedCv = documentRepository.save(cvDocument);

        UserProfile profile = new UserProfile();
        profile.setUserAccount(saved);
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setClassYear(request.classYear());
        profile.setDepartment(request.department());
        profile.setEnglishLevel(request.englishLevel());
        profile.setGpa(request.gpa());
        profile.setCvDocument(savedCv);
        userProfileRepository.save(profile);

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(saved);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        emailVerificationTokenRepository.save(verificationToken);
        authMailStubService.sendVerificationMail(saved.getEmail(), verificationToken.getToken());
        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getRole().name());
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid token"));
        if (verificationToken.getUsedAt() != null) {
            throw new BadRequestException("Token already used");
        }
        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Token expired");
        }
        UserAccount user = verificationToken.getUser();
        user.setEmailVerified(true);
        user.setEnabled(true);
        verificationToken.setUsedAt(Instant.now());
        userAccountRepository.save(user);
        emailVerificationTokenRepository.save(verificationToken);
        return new MessageResponse("Email verified");
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = emailDomainPolicy.normalize(request.email());
        userAccountRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
            passwordResetTokenRepository.save(resetToken);
            authMailStubService.sendPasswordResetMail(user.getEmail(), resetToken.getToken());
        });
        return new MessageResponse("If email exists, reset instructions are sent");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid token"));
        if (resetToken.getUsedAt() != null) {
            throw new BadRequestException("Token already used");
        }
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Token expired");
        }
        UserAccount user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsedAt(Instant.now());
        userAccountRepository.save(user);
        passwordResetTokenRepository.save(resetToken);
        return new MessageResponse("Password reset successful");
    }

}
