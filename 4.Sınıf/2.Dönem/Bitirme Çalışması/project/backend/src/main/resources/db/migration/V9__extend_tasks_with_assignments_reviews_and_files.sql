ALTER TABLE project_tasks
    ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;

UPDATE project_tasks
SET created_by_user_id = (
    SELECT u.id
    FROM users u
    WHERE u.role = 'ADMIN'
    ORDER BY u.id
    LIMIT 1
)
WHERE created_by_user_id IS NULL;

ALTER TABLE project_tasks
    ALTER COLUMN created_by_user_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_project_tasks_created_by_user'
    ) THEN
        ALTER TABLE project_tasks
            ADD CONSTRAINT fk_project_tasks_created_by_user
                FOREIGN KEY (created_by_user_id) REFERENCES users (id);
    END IF;
END$$;

ALTER TABLE task_assignments
    ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS review_note VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS text_answer VARCHAR(5000);

UPDATE task_assignments
SET assigned_at = COALESCE(assigned_at, created_at, NOW())
WHERE assigned_at IS NULL;

ALTER TABLE task_assignments
    ALTER COLUMN assigned_at SET NOT NULL;

CREATE TABLE IF NOT EXISTS task_attachments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_task_attachments_task
        FOREIGN KEY (task_id) REFERENCES project_tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_attachments_document
        FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT uk_task_attachments_task_document UNIQUE (task_id, document_id)
);

CREATE TABLE IF NOT EXISTS task_submission_files (
    id BIGSERIAL PRIMARY KEY,
    task_assignment_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_task_submission_files_assignment
        FOREIGN KEY (task_assignment_id) REFERENCES task_assignments (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_submission_files_document
        FOREIGN KEY (document_id) REFERENCES documents (id) ON DELETE CASCADE,
    CONSTRAINT uk_task_submission_files_assignment_document UNIQUE (task_assignment_id, document_id)
);

CREATE INDEX IF NOT EXISTS idx_task_assignments_user_status ON task_assignments (user_id, status);
CREATE INDEX IF NOT EXISTS idx_task_assignments_task_status ON task_assignments (task_id, status);
CREATE INDEX IF NOT EXISTS idx_task_attachments_task_id ON task_attachments (task_id);
CREATE INDEX IF NOT EXISTS idx_task_submission_files_assignment_id ON task_submission_files (task_assignment_id);
