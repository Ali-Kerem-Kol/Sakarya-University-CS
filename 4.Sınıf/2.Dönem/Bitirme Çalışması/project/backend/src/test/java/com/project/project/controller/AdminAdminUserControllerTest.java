package com.project.project.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.dto.admin.AdminUserCreateResponse;
import com.project.project.security.JwtAuthenticationFilter;
import com.project.project.service.admin.AdminUserService;

@WebMvcTest(AdminAdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminAdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createAdminReturnsCreated() throws Exception {
        when(adminUserService.createAdminUser(any())).thenReturn(
                new AdminUserCreateResponse(11L, "newadmin@32bit.com.tr", "ADMIN", true, true, "Ada", "Lovelace")
        );

        mockMvc.perform(post("/api/v1/admin/admin-users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"newadmin@32bit.com.tr",
                                  "password":"Admin12345!",
                                  "firstName":"Ada",
                                  "lastName":"Lovelace"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newadmin@32bit.com.tr"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
