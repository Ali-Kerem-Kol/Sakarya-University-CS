package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.auth.MessageResponse;
import com.project.project.dto.user.ChangePasswordRequest;
import com.project.project.dto.user.MyAccountResponse;
import com.project.project.dto.user.UpdateMyEmailRequest;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.MyAccountService;

/**
 * Endpoints for authenticated account settings (email/password).
 */
@RestController
@RequestMapping("/api/v1/me")
public class MyAccountController {

    private final MyAccountService myAccountService;

    public MyAccountController(MyAccountService myAccountService) {
        this.myAccountService = myAccountService;
    }

    @GetMapping
    public ResponseEntity<MyAccountResponse> getMyAccount(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(myAccountService.getAccount(principal.getUserAccount().getId()));
    }

    @PutMapping("/email")
    public ResponseEntity<MyAccountResponse> updateEmail(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody UpdateMyEmailRequest request
    ) {
        return ResponseEntity.ok(myAccountService.updateEmail(principal.getUserAccount().getId(), request.email()));
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserAccountPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        myAccountService.changePassword(
                principal.getUserAccount().getId(),
                request.currentPassword(),
                request.newPassword()
        );
        return ResponseEntity.ok(new MessageResponse("Password changed"));
    }
}
