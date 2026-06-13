CREATE INDEX IF NOT EXISTS idx_application_postings_category_status
    ON application_postings (category, status);

CREATE INDEX IF NOT EXISTS idx_application_submissions_posting_user_submitted
    ON application_submissions (posting_id, user_id, submitted_at DESC);
