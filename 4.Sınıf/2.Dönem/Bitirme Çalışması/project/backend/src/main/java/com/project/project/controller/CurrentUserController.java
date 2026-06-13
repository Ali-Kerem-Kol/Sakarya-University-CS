package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.dto.user.MyAccountResponse;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.MyAccountService;

/**
 * Compatibility endpoints for current user/account info across role-specific routes.
 */
@RestController
public class CurrentUserController {

    private final MyAccountService myAccountService;

    public CurrentUserController(MyAccountService myAccountService) {
        this.myAccountService = myAccountService;
    }

    @GetMapping("/api/v1/users/me")
    public ResponseEntity<MyAccountResponse> currentUser(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(myAccountService.getAccount(principal.getUserAccount().getId()));
    }

    @GetMapping("/api/v1/admin/me")
    public ResponseEntity<MyAccountResponse> currentAdmin(
            @AuthenticationPrincipal UserAccountPrincipal principal
    ) {
        return ResponseEntity.ok(myAccountService.getAccount(principal.getUserAccount().getId()));
    }
}
