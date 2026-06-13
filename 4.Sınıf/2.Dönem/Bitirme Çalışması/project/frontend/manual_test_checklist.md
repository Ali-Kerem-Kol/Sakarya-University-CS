# Manual Test Checklist

Use this checklist to verify the frontend application against the Backend API Contract.

## 1. Authentication
- [ ] **Register**: Fill all fields. Upload PDF CV (<50MB). Submit.
  - Verify payload: `multipart/form-data` with `data` (JSON) and `cv` (File).
- [ ] **Login**: Login with student email (`@ogr.sakarya.edu.tr`) or admin (`@32bit.com.tr`).
- [ ] **Verify**: Access `/verify?token=...`. Check success/fail messages.
- [ ] **Forgot Password**: Request reset link.
- [ ] **Reset Password**: Use token to set new password.

## 2. User Area (Candidate)
- [ ] **Profile**: Update First Name / Last Name.
- [ ] **Email**: Change email. Must provide **current password**.
- [ ] **Password**: Change password.
- [ ] **Documents**:
  - [ ] View list (CV, Schedule).
  - [ ] Upload Schedule (.xlsx).
  - [ ] Download CV and Schedule (verifying path `/users/me/documents/{id}/download`).
- [ ] **Public Postings**:
  - [ ] View list. Filter by Category.
  - [ ] Closed postings should be visible but "Applications Closed".
- [ ] **Apply**:
  - [ ] Click Apply on a PUBLISHED posting (`POST /me/submissions`).
  - [ ] Check "Already Applied" if repeated.

## 3. Admin Area
- [ ] **Postings**:
  - [ ] Create DRAFT.
  - [ ] Edit DRAFT/PUBLISHED.
  - [ ] Publish DRAFT.
  - [ ] Close PUBLISHED.
  - [ ] **Attachments**:
    - [ ] Add files to DRAFT/PUBLISHED.
    - [ ] Verify "Upload" is disabled for CLOSED postings.
    - [ ] Delete attachments.
- [ ] **Submissions**:
  - [ ] View list. Filter by Category / Status.
- [ ] **Mail Operations**:
  - [ ] Send to Category (e.g. BACKEND).
  - [ ] Send to Posting (select a job).
  - [ ] Send to All Students.
  - [ ] Verify multipart payload (JSON data + optional files).
- [ ] **User Management**:
  - [ ] Create new Admin (Email, Password, Name).
  - [ ] Verify `@32bit.com.tr` restriction.

## 4. Error Handling
- [ ] **File Too Large**: Upload >50MB file. Expect alert/toast message (413).
- [ ] **Forbidden**: Access Admin page as User. Expect 403 or redirect.
