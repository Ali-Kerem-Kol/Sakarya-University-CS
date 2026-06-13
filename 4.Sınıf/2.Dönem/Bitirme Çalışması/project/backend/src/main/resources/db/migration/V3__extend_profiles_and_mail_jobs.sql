ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS class_year INT NULL;

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS department VARCHAR(120) NULL;

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS english_level VARCHAR(40) NULL;

ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS gpa NUMERIC(3,2) NULL;

ALTER TABLE mail_jobs
    ADD COLUMN IF NOT EXISTS error_message VARCHAR(1000) NULL;

CREATE TABLE IF NOT EXISTS mail_job_attachments (
    id BIGSERIAL PRIMARY KEY,
    mail_job_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_mail_job_attachments_job
        FOREIGN KEY (mail_job_id) REFERENCES mail_jobs (id) ON DELETE CASCADE,
    CONSTRAINT fk_mail_job_attachments_document
        FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_mail_job_attachments_job_id ON mail_job_attachments (mail_job_id);
