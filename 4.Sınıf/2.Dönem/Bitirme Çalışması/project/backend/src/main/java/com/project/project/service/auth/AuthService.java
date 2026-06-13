package com.project.project.service.auth;

import com.project.project.dto.auth.LoginRequest;
import com.project.project.dto.auth.LoginResponse;
import com.project.project.dto.auth.MessageResponse;
import com.project.project.dto.auth.RegisterRequest;
import com.project.project.dto.auth.RegisterResponse;
import com.project.project.dto.auth.ForgotPasswordRequest;
import com.project.project.dto.auth.ResetPasswordRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * Defines authentication operations for issuing JWT tokens.
 */
public interface AuthService {

    LoginResponse authenticate(LoginRequest request);

    RegisterResponse register(RegisterRequest request, MultipartFile cvFile);

    MessageResponse verifyEmail(String token);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);
}
