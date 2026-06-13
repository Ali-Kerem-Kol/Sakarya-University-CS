package com.project.project.controller;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.config.exception.AvailabilityOverlapException;
import com.project.project.dto.user.AvailabilitySlotResponse;
import com.project.project.entity.Role;
import com.project.project.entity.UserAccount;
import com.project.project.security.UserAccountPrincipal;
import com.project.project.service.user.UserAvailabilityService;

/**
 * Verifies availability endpoints for user operations.
 */
@WebMvcTest(UserAvailabilityController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAvailabilityService userAvailabilityService;

    @Test
    void createSlotSuccessReturnsResponse() throws Exception {
        AvailabilitySlotResponse response = new AvailabilitySlotResponse(
                1L,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                "Europe/Istanbul",
                Instant.now()
        );
        when(userAvailabilityService.createSlot(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(response);

        String payload = """
                {
                  "dayOfWeek": "MONDAY",
                  "startTime": "09:00",
                  "endTime": "12:00"
                }
                """;

        mockMvc.perform(post("/api/v1/users/me/availability")
                        .contentType("application/json")
                        .content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.timezone").value("Europe/Istanbul"));
    }

    @Test
    void createSlotOverlapReturnsConflict() throws Exception {
        when(userAvailabilityService.createSlot(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any()
        )).thenThrow(new AvailabilityOverlapException("Overlap"));

        String payload = """
                {
                  "dayOfWeek": "MONDAY",
                  "startTime": "10:00",
                  "endTime": "11:00"
                }
                """;

        mockMvc.perform(post("/api/v1/users/me/availability")
                        .contentType("application/json")
                        .content(payload)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("AVAILABILITY_OVERLAP"));
    }

    @Test
    void deleteOtherUsersSlotReturnsForbidden() throws Exception {
        doThrow(new org.springframework.security.access.AccessDeniedException("Forbidden"))
                .when(userAvailabilityService).deleteSlot(1L, 5L);

        mockMvc.perform(delete("/api/v1/users/me/availability/5")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    private UsernamePasswordAuthenticationToken authentication() {
        UserAccount account = new UserAccount();
        account.setId(1L);
        account.setEmail("user@example.com");
        account.setRole(Role.USER);
        UserAccountPrincipal principal = new UserAccountPrincipal(account);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
