CREATE TABLE IF NOT EXISTS timeline_events (
    id BIGSERIAL PRIMARY KEY,
    scope_type VARCHAR(20) NOT NULL,
    scope_id BIGINT NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    actor_user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    assignment_id BIGINT NULL,
    payload_json VARCHAR(4000),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_timeline_events_actor_user
        FOREIGN KEY (actor_user_id) REFERENCES users (id),
    CONSTRAINT fk_timeline_events_task
        FOREIGN KEY (task_id) REFERENCES project_tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_timeline_events_assignment
        FOREIGN KEY (assignment_id) REFERENCES task_assignments (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_timeline_events_scope_created_at
    ON timeline_events (scope_type, scope_id, created_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_timeline_events_task_id ON timeline_events (task_id);
CREATE INDEX IF NOT EXISTS idx_timeline_events_assignment_id ON timeline_events (assignment_id);
