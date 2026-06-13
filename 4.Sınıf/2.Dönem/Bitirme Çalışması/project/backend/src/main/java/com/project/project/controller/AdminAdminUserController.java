package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.project.project.dto.admin.AdminUserCreateResponse;
import com.project.project.dto.admin.CreateAdminUserRequest;
import com.project.project.service.admin.AdminUserService;

/**
 * Admin-only endpoints for admin account creation.
 */
@RestController
@RequestMapping("/api/v1/admin/admin-users")
public class AdminAdminUserController {

    private final AdminUserService adminUserService;

    public AdminAdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<AdminUserCreateResponse> createAdminUser(
            @Valid @RequestBody CreateAdminUserRequest request
    ) {
        return ResponseEntity.status(201).body(adminUserService.createAdminUser(request));
    }
}
