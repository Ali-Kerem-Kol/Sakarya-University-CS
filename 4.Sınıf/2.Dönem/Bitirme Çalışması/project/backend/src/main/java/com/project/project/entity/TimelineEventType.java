package com.project.project.entity;

/**
 * Event type emitted by task/assignment workflow.
 */
public enum TimelineEventType {
    TASK_CREATED,
    TASK_ASSIGNED_ALL,
    TASK_ASSIGNED_USER,
    TASK_SUBMITTED,
    TASK_REVIEWED_APPROVED,
    TASK_REVIEWED_REJECTED,
    TASK_REVIEWED_REVISION
}
