package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.project.dto.auth.ForgotPasswordRequest;
import com.project.project.dto.auth.LoginRequest;
import com.project.project.dto.auth.LoginResponse;
import com.project.project.dto.auth.MessageResponse;
import com.project.project.dto.auth.RegisterRequest;
import com.project.project.dto.auth.RegisterResponse;
import com.project.project.dto.auth.ResetPasswordRequest;
import com.project.project.service.auth.AuthService;

/**
 * Exposes authentication endpoints for JWT login.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestPart("data") RegisterRequest request,
            @RequestPart(value = "cv", required = false) MultipartFile cv
    ) {
        log.info(
                "Register request received: email={}, cvPresent={}, cvContentType={}, cvSize={}",
                request.email(),
                cv != null && !cv.isEmpty(),
                cv != null ? cv.getContentType() : null,
                cv != null ? cv.getSize() : null
        );
        return ResponseEntity.status(201).body(authService.register(request, cv));
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
