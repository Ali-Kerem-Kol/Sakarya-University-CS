-- Submission status migration to pending-based workflow
UPDATE application_submissions
SET status = 'PENDING'
WHERE status IN ('SUBMITTED', 'IN_REVIEW');

-- Tasks
CREATE TABLE IF NOT EXISTS project_tasks (
    id BIGSERIAL PRIMARY KEY,
    posting_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(5000) NOT NULL,
    due_date DATE NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_project_tasks_posting
        FOREIGN KEY (posting_id) REFERENCES application_postings (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task_assignments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    note VARCHAR(1000) NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_task_assignments_task
        FOREIGN KEY (task_id) REFERENCES project_tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_assignments_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_task_assignment_task_user UNIQUE (task_id, user_id)
);

-- Q&A
CREATE TABLE IF NOT EXISTS posting_questions (
    id BIGSERIAL PRIMARY KEY,
    posting_id BIGINT NOT NULL,
    asked_by_user_id BIGINT NOT NULL,
    question_text VARCHAR(5000) NOT NULL,
    publish_scope VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_posting_questions_posting
        FOREIGN KEY (posting_id) REFERENCES application_postings (id) ON DELETE CASCADE,
    CONSTRAINT fk_posting_questions_user
        FOREIGN KEY (asked_by_user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS question_answers (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL UNIQUE,
    answered_by_admin_id BIGINT NOT NULL,
    answer_text VARCHAR(5000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_question_answers_question
        FOREIGN KEY (question_id) REFERENCES posting_questions (id) ON DELETE CASCADE,
    CONSTRAINT fk_question_answers_admin
        FOREIGN KEY (answered_by_admin_id) REFERENCES users (id)
);

-- Announcements
CREATE TABLE IF NOT EXISTS announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(10000) NOT NULL,
    created_by_admin_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_announcements_admin
        FOREIGN KEY (created_by_admin_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_project_tasks_posting_id ON project_tasks (posting_id);
CREATE INDEX IF NOT EXISTS idx_task_assignments_user_id ON task_assignments (user_id);
CREATE INDEX IF NOT EXISTS idx_posting_questions_posting_scope ON posting_questions (posting_id, publish_scope);
CREATE INDEX IF NOT EXISTS idx_announcements_created_at ON announcements (created_at DESC);

