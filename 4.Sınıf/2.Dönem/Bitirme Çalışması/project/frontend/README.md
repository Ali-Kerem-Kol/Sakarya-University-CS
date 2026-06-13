# ATS Frontend

University Internship Application Tracking System Frontend.

## Overview
This is the frontend application for the ATS, built with:
- React 18+ (Vite)
- TypeScript
- Tailwind CSS
- Shadcn/UI
- TanStack Query (React Query)
- Axios

## Prerequisites
- Node.js 18+
- Backend API running at http://localhost:8080 (or configured via env)

## Environment Variables
Create a `.env` file in the root if needed, or set variables in your environment.

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_BASE` | Base URL for the Backend API | `/api/v1` |

## Running Locally

1. Install dependencies:
   ```bash
   npm install
   ```
2. Start development server:
   ```bash
   npm run dev
   ```
   Access at `http://localhost:5173`.

## Docker
The app is served via Nginx in production/docker.

```bash
docker build -t frontend .
docker run -p 3000:80 frontend
```
Ensure your Nginx config directs `/api` requests to your backend (default assumes `http://backend:8080` in docker-compose).

## Manual Test Checklist

### 1. Public Flows
- [ ] **Landing Page**: View all postings (PUBLISHED + CLOSED). Filter by category using Tabs.
- [ ] **Posting Detail**: Click a posting. Expect to see descriptions.
- [ ] **Apply Button**: Click "Apply Now". If not logged in, redirects to Login.
- [ ] **File Download**: If posting has attachments, click download. Should work.

### 2. Authentication
- [ ] **Register**: Fill form (valid email @ogr...). Upload PDF CV (<50MB). Submit. Expect success message/redirect.
- [ ] **Login**: Login with verified credentials. Redirects to User Dashboard.
- [ ] **Admin Login**: Login with admin credentials. Redirects to Admin Dashboard.
- [ ] **Verify**: Visit `/verify?token=INVALID` (Error), `/verify?token=VALID` (Success).
- [ ] **Forgot Password**: Request reset link.

### 3. User Dashboard
- [ ] **Profile**: Update First Name/Last Name. Refresh to verify.
- [ ] **Security**: Change Password. Change Email.
- [ ] **Documents**: Upload Schedule (.xlsx). Check 413 error with large file (>50MB).
- [ ] **My Applications**: View list of applied jobs. Check status badge.

### 4. Admin Dashboard
- [ ] **Postings**: Create new Draft. Edit Draft. Publish Draft (check visibility on Landing). Close published posting.
- [ ] **Submissions**: Click "View Submissions" on a posting. See list of applicants.
- [ ] **Mail**: Send email to "Backends". View Toast confirmation.
- [ ] **Applicant Detail**: Open Applicant Drawer. View uploaded docs.

### 5. Error Handling
- [ ] **413**: Upload file > 50MB. Expect Toast "File too large".
- [ ] **401**: Expire token manually (clear LocalStorage). Refresh. Expect Redirect to Login.
- [ ] **409**: Apply to same job twice. Expect Toast "Already Applied".
