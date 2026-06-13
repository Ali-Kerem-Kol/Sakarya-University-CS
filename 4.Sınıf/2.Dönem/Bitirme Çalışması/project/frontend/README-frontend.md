# Frontend Refactor Summary

This document summarizes the changes made to the frontend to ensure compliance with the Backend API Contract.

## 1. API Layer (`src/api/`)
- **`client.ts`**: Configured with `VITE_API_BASE` (default `/api/v1`). Added request interceptor for `Authorization: Bearer token`. Added response interceptors for global error handling:
  - `401`: Redirect to login.
  - `403`: Access denied alert.
  - `413`: File too large alert.
- **`auth.ts`**: Centralized authentication functions matching endpoints:
  - `POST /auth/register` (multipart: `data` JSON + `cv` File).
  - `POST /auth/login`, `GET /auth/verify`, `POST /auth/forgot-password`, `POST /auth/reset-password`.
- **`user.ts`**:
  - `GET /users/me/documents`, `POST /users/me/documents/cv`, `POST /users/me/documents/schedule`.
  - `PUT /me/profile`, `/me/email`, `/me/password`.
  - `POST /me/submissions` (Checks for `409` in components).
- **`admin.ts`**:
  - Mail functions: `createMailJobByCategory`, `createMailJobByPosting`, `createMailJobAllStudents` (multipart).
  - Postings: `create`, `update`, `publish`, `close`, `attachments`.
  - Users: `createAdminUser` (email, password, firstName, lastName).
  - Submissions: `fetchAdminSubmissions`.

## 2. Pages & Features

### Authentication
- **Register (`RegisterPage.tsx`)**: 
  - Validates inputs including strictly PDF for CV and `<50MB` size.
  - Constructs `multipart/form-data` correctly.
- **Login/Verify/Reset**: Implemented and linked.

### User Area (Candidate)
- **Documents (`UserDocumentsPage.tsx`)**:
  - Lists CV and Schedule.
  - Allows Schedule upload (`.xlsx`).
  - Downloads using `/users/me/documents/{id}/download`.
- **Profile (`UserProfilePage.tsx`)**:
  - Edit Profile, Email (requires current password), Password.
- **Submissions (`UserSubmissionsPage.tsx`)**:
  - Lists user's applications.
- **Applications (`UserApplicationsPage.tsx`)**: (Assuming alias/redirect)

### Admin Area
- **Postings (`AdminPostingsPage.tsx`)**:
  - CRUD for Postings.
  - **Logic**: Attachment upload is DISABLED when status is `CLOSED`.
- **Submissions (`AdminSubmissionsPage.tsx`)**:
  - Filter by Category, Status, Posting Status.
  - CV Download using `cvDownloadUrl` from backend.
- **Mail (`AdminMailPage.tsx`)**:
  - Three distinct tabs for sending mail (Category, Posting, All).
  - Supports attachments.
- **Users (`AdminUsersPage.tsx`)**:
  - Create new Admin with full details. Enforces `@32bit.com.tr` domain validation in UI.

## 3. Routing & Guards
- **`RouteGuards.tsx`**:
  - `ProtectedRoute`: Checks `isAuthenticated`.
  - `AdminRoute`: Checks `isAuthenticated` AND `isAdmin`.
- **`App.tsx`**:
  - `/user/*` routes protected.
  - `/admin/*` routes protected + admin check.

## 4. Verification Checklist
- [x] Register with CV (PDF).
- [x] Login as User / Admin.
- [x] User: Upload Schedule.
- [x] User: Apply to Published Posting.
- [x] Admin: Create/Publish Posting.
- [x] Admin: Close Posting (Verify inputs disabled).
- [x] Admin: Send Mail (Multipart).
- [x] Admin: Create new Admin.
