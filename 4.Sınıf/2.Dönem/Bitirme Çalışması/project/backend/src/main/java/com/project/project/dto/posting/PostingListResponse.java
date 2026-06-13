package com.project.project.dto.posting;

import java.util.List;

/**
 * List wrapper for postings.
 * Keeps both "postings" and "content" keys for compatibility.
 */
public class PostingListResponse {

    private final List<PostingResponse> postings;
    private final List<PostingResponse> content;

    public PostingListResponse(List<PostingResponse> postings) {
        this.postings = postings;
        this.content = postings;
    }

    public List<PostingResponse> getPostings() {
        return postings;
    }

    public List<PostingResponse> getContent() {
        return content;
    }
}
