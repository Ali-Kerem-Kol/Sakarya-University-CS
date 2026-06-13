package com.project.project.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import com.project.project.config.GlobalExceptionHandler;
import com.project.project.service.policy.PublicDownloadRateLimitHook;
import com.project.project.service.posting.PostingAttachmentService;

@WebMvcTest(PostingAttachmentDownloadController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PostingAttachmentDownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostingAttachmentService postingAttachmentService;

    @MockBean
    private PublicDownloadRateLimitHook publicDownloadRateLimitHook;

    @Test
    void draftAttachmentBlockedForPublic() throws Exception {
        when(postingAttachmentService.download(eq(10L), eq(88L), eq((Long) null)))
                .thenThrow(new AccessDeniedException("Draft attachments are admin-only"));

        mockMvc.perform(get("/api/v1/postings/10/attachments/88/download"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publishedAttachmentDownloadSuccess() throws Exception {
        PostingAttachmentService.AttachmentDownload download = new PostingAttachmentService.AttachmentDownload(
                new ByteArrayResource("ok".getBytes(StandardCharsets.UTF_8)),
                "application/pdf",
                "doc.pdf"
        );
        when(postingAttachmentService.download(eq(10L), eq(89L), eq((Long) null))).thenReturn(download);

        mockMvc.perform(get("/api/v1/postings/10/attachments/89/download"))
                .andExpect(status().isOk());
    }
}
