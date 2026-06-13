package com.project.project.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.project.entity.UserAccount;
import com.project.project.entity.UserProfile;
import com.project.project.repository.UserAccountRepository;
import com.project.project.repository.UserProfileRepository;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.storage.root=./target/test-storage",
        "management.health.mail.enabled=false"
})
class RegisterMultipartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void registerMultipartCreatesDisabledUserProfileAndCvLink() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "email":"student1@ogr.sakarya.edu.tr",
                  "password":"Password123",
                  "firstName":"Ali",
                  "lastName":"Kaya",
                  "classYear":3,
                  "department":"Computer Engineering",
                  "englishLevel":"B2",
                  "gpa":3.25
                }
                """.getBytes()
        );
        MockMultipartFile cv = new MockMultipartFile(
                "cv",
                "cv.pdf",
                "application/pdf",
                "dummy-pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/register").file(data).file(cv))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("student1@ogr.sakarya.edu.tr"));

        UserAccount account = userAccountRepository.findByEmailIgnoreCase("student1@ogr.sakarya.edu.tr").orElseThrow();
        assertThat(account.isEnabled()).isFalse();
        assertThat(account.isEmailVerified()).isFalse();

        UserProfile profile = userProfileRepository.findByUserAccountId(account.getId()).orElseThrow();
        assertThat(profile.getFirstName()).isEqualTo("Ali");
        assertThat(profile.getClassYear()).isEqualTo(3);
        assertThat(profile.getCvDocument()).isNotNull();

        account.setEnabled(true);
        account.setEmailVerified(true);
        userAccountRepository.save(account);

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"student1@ogr.sakarya.edu.tr","password":"Password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/users/me/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasCv").value(true))
                .andExpect(jsonPath("$.cvFileName").value("cv.pdf"))
                .andExpect(jsonPath("$.cvDownloadUrl").isNotEmpty());
    }

    @Test
    void registerWithoutCvReturnsCvRequired() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "email":"student2@ogr.sakarya.edu.tr",
                  "password":"Password123",
                  "firstName":"Veli",
                  "lastName":"Demir",
                  "classYear":2,
                  "department":"Math",
                  "englishLevel":"B1",
                  "gpa":2.80
                }
                """.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/register").file(data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CV_REQUIRED"));
    }

    @Test
    void registerWrongDomainReturnsInvalidEmailDomain() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "email":"student@gmail.com",
                  "password":"Password123",
                  "firstName":"Ayse",
                  "lastName":"Yilmaz",
                  "classYear":4,
                  "department":"Physics",
                  "englishLevel":"C1",
                  "gpa":3.70
                }
                """.getBytes()
        );
        MockMultipartFile cv = new MockMultipartFile(
                "cv",
                "cv.pdf",
                "application/pdf",
                "dummy-pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/register").file(data).file(cv))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_EMAIL_DOMAIN"));
    }

    @Test
    void registerMalformedMultipartJsonReturnsInvalidJson() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {"email":"broken@ogr.sakarya.edu.tr","password":
                """.getBytes()
        );
        MockMultipartFile cv = new MockMultipartFile(
                "cv",
                "cv.pdf",
                "application/pdf",
                "dummy-pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/register").file(data).file(cv))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MULTIPART_JSON"))
                .andExpect(jsonPath("$.code").value("INVALID_MULTIPART_JSON"))
                .andExpect(jsonPath("$.message").value("Malformed JSON in multipart field 'data'"));
    }

    @Test
    void registerCommaDecimalGpaIsAccepted() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "email":"student3@ogr.sakarya.edu.tr",
                  "password":"Password123",
                  "firstName":"Mehmet",
                  "lastName":"Yildiz",
                  "classYear":"3",
                  "department":"Computer Engineering",
                  "englishLevel":"B2",
                  "gpa":"3,5"
                }
                """.getBytes()
        );
        MockMultipartFile cv = new MockMultipartFile(
                "cv",
                "cv.pdf",
                "application/pdf",
                "dummy-pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/register").file(data).file(cv))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("student3@ogr.sakarya.edu.tr"));

        UserAccount account = userAccountRepository.findByEmailIgnoreCase("student3@ogr.sakarya.edu.tr").orElseThrow();
        UserProfile profile = userProfileRepository.findByUserAccountId(account.getId()).orElseThrow();
        assertThat(profile.getGpa()).isEqualByComparingTo("3.50");
        assertThat(profile.getClassYear()).isEqualTo(3);
    }

    @Test
    void registerNonNumericGpaReturnsFieldFormatError() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "email":"student4@ogr.sakarya.edu.tr",
                  "password":"Password123",
                  "firstName":"Asli",
                  "lastName":"Can",
                  "classYear":3,
                  "department":"Math",
                  "englishLevel":"B1",
                  "gpa":"NaN"
                }
                """.getBytes()
        );
        MockMultipartFile cv = new MockMultipartFile(
                "cv",
                "cv.pdf",
                "application/pdf",
                "dummy-pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/register").file(data).file(cv))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REGISTER_FIELD_FORMAT"))
                .andExpect(jsonPath("$.fieldErrors.gpa").value("gpa must be numeric (comma and dot decimals are supported)"));
    }

    @Test
    void registerClassYearOutOfRangeReturnsValidationError() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "email":"student5@ogr.sakarya.edu.tr",
                  "password":"Password123",
                  "firstName":"Ece",
                  "lastName":"Akin",
                  "classYear":-1,
                  "department":"Math",
                  "englishLevel":"B1",
                  "gpa":2.5
                }
                """.getBytes()
        );
        MockMultipartFile cv = new MockMultipartFile(
                "cv",
                "cv.pdf",
                "application/pdf",
                "dummy-pdf".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/auth/register").file(data).file(cv))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.classYear").value("classYear must be between 1 and 8"));
    }
}
