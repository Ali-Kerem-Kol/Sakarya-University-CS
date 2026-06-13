CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    checksum VARCHAR(128),
    uploaded_by_user_id BIGINT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_documents_uploaded_by_user
        FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS application_postings (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    project_name VARCHAR(200) NOT NULL,
    project_details VARCHAR(5000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by_admin_id BIGINT NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NULL,
    closed_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_postings_created_by_admin
        FOREIGN KEY (created_by_admin_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS posting_attachments (
    id BIGSERIAL PRIMARY KEY,
    posting_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    attached_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_posting_attachments_posting
        FOREIGN KEY (posting_id) REFERENCES application_postings (id) ON DELETE CASCADE,
    CONSTRAINT fk_posting_attachments_document
        FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS application_submissions (
    id BIGSERIAL PRIMARY KEY,
    posting_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL,
    profile_snapshot_json JSONB NOT NULL,
    snapshot_version INT NOT NULL,
    cv_document_id_snapshot BIGINT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_submissions_posting
        FOREIGN KEY (posting_id) REFERENCES application_postings (id) ON DELETE CASCADE,
    CONSTRAINT fk_submissions_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_submission_posting_user UNIQUE (posting_id, user_id)
);

CREATE TABLE IF NOT EXISTS mail_jobs (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payload_json VARCHAR(2000),
    created_by_admin_id BIGINT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_mail_jobs_created_by_admin
        FOREIGN KEY (created_by_admin_id) REFERENCES users (id)
);

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS cv_document_id BIGINT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_user_profiles_cv_document'
    ) THEN
        ALTER TABLE user_profiles
            ADD CONSTRAINT fk_user_profiles_cv_document
                FOREIGN KEY (cv_document_id) REFERENCES documents (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_application_postings_category ON application_postings (category);
CREATE INDEX IF NOT EXISTS idx_application_postings_status ON application_postings (status);
CREATE INDEX IF NOT EXISTS idx_application_submissions_posting_id ON application_submissions (posting_id);
CREATE INDEX IF NOT EXISTS idx_application_submissions_user_id ON application_submissions (user_id);
