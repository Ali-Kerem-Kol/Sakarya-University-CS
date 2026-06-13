package com.project.project.service.user;

import com.project.project.dto.user.MyAccountResponse;

/**
 * Authenticated self-account operations.
 */
public interface MyAccountService {

    MyAccountResponse getAccount(Long userId);

    MyAccountResponse updateEmail(Long userId, String rawEmail);

    void changePassword(Long userId, String currentPassword, String newPassword);
}
