# API Contract V3

## Posting Visibility
- Public list/detail: only `PUBLISHED`
  - `GET /api/v1/public/postings`
  - `GET /api/v1/public/postings/{id}`
- Admin status actions:
  - `POST /api/v1/admin/postings/{id}/publish`
  - `POST /api/v1/admin/postings/{id}/close`
  - `POST /api/v1/admin/postings/{id}/reopen`

## Submission Workflow
- User create submission:
  - `POST /api/v1/me/submissions`
  - Request: `{ "postingId": 12 }`
  - Response status is `PENDING`
- Admin decisions:
  - `POST /api/v1/admin/submissions/{id}/accept`
  - `POST /api/v1/admin/submissions/{id}/reject`
  - `POST /api/v1/admin/submissions/{id}/remove`

### Membership model
- No extra `project_members` table.
- Membership is derived from `application_submissions.status = ACCEPTED`.

## Task Management
- Admin:
  - `POST /api/v1/admin/postings/{postingId}/tasks`
  - `GET /api/v1/admin/postings/{postingId}/tasks`
  - `PUT /api/v1/admin/tasks/{taskId}`
  - `DELETE /api/v1/admin/tasks/{taskId}`
  - `POST /api/v1/admin/tasks/{taskId}/assign/{userId}`
  - `POST /api/v1/admin/tasks/{taskId}/mark-done/{userId}`
- User:
  - `GET /api/v1/me/tasks`

## Q&A
- User:
  - `POST /api/v1/me/postings/{postingId}/questions`
  - `GET /api/v1/me/questions`
- Admin:
  - `GET /api/v1/admin/postings/{postingId}/questions`
  - `POST /api/v1/admin/questions/{id}/answer`
  - `POST /api/v1/admin/questions/{id}/publish`
- Public feed:
  - `GET /api/v1/public/questions` (scope: `PUBLIC`)
- Project-only feed:
  - `GET /api/v1/postings/{postingId}/questions` (scope: `PROJECT_ONLY`, requester must have submission)

## Announcements
- Public:
  - `GET /api/v1/public/announcements`
- Admin:
  - `POST /api/v1/admin/announcements`
  - `PUT /api/v1/admin/announcements/{id}`
  - `DELETE /api/v1/admin/announcements/{id}`

## Example Requests
```http
POST /api/v1/admin/submissions/44/accept
Authorization: Bearer <ADMIN_TOKEN>
```

```http
POST /api/v1/admin/tasks/9/assign/31
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{"note":"Sprint-1 deliverable"}
```

```http
POST /api/v1/admin/questions/55/publish
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{"scope":"PROJECT_ONLY"}
```

