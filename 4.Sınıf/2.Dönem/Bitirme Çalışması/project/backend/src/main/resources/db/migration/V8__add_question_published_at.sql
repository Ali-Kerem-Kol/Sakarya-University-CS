ALTER TABLE posting_questions
    ADD COLUMN IF NOT EXISTS published_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_posting_questions_posting_scope_published
    ON posting_questions (posting_id, publish_scope, published_at DESC);

