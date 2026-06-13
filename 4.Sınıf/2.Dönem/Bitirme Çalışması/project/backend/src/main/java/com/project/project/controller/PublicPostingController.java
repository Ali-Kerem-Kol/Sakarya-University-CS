package com.project.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.project.dto.posting.PostingListResponse;
import com.project.project.dto.posting.PostingResponse;
import com.project.project.entity.ApplicationCategory;
import com.project.project.service.posting.PublicPostingService;

/**
 * Public endpoints for published postings.
 */
@RestController
@RequestMapping("/api/v1/public/postings")
public class PublicPostingController {

    private final PublicPostingService publicPostingService;

    public PublicPostingController(PublicPostingService publicPostingService) {
        this.publicPostingService = publicPostingService;
    }

    @GetMapping
    public ResponseEntity<PostingListResponse> listPublished(
            @RequestParam(required = false) ApplicationCategory category
    ) {
        return ResponseEntity.ok(publicPostingService.listPublished(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostingResponse> getPublished(@PathVariable Long id) {
        return ResponseEntity.ok(publicPostingService.getPublished(id));
    }
}
