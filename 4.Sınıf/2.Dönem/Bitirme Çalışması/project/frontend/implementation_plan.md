
# ATS Frontend Refinement Implementation Plan

This document outlines the changes made to refine the ATS frontend, specifically focusing on the User Profile, API integration, and general code quality.

## 1. User Profile & Settings
- **`UserProfilePage.tsx`**: 
  - Refactored to use standardized API functions from `api/user.ts`.
  - Fixed linting errors related to `useForm` types and unused variables.
  - Ensured password and email update forms correctly map to the backend API contracts (e.g., passing `{ oldPassword, newPassword }`).
  - Improved error handling and UI feedback (toasts).
- **`UserDocumentsPage.tsx`**:
  - Implemented functionality to fetch and display user documents (CV, Schedule).
  - Added "Upload Schedule" feature using the correct API endpoint (`/me/documents/schedule`).
  - Added to the application routing (`/user/documents`) and navigation menu.

## 2. API Layer Improvements
- **`api/user.ts`**:
  - Added `fetchMyProfile` for consistent profile data fetching.
  - Added `fetchMyDocuments` and `uploadSchedule` for document management.
  - Updated `updateMyEmail` and `updateMyPassword` to accept structured payloads matching backend requirements.
  - Removed unused `downloadFile` import where not needed.
- **`api/admin.ts`**:
  - Updated `createMailJob` to support file attachments and complex JSON payloads using `FormData` and `Blob`, aligned with typical backend multi-part requests.
  - Consolidated admin-related API calls.
- **`api/postings.ts`**: 
  - Verified public posting fetch and detail retrieval.

## 3. Admin Module Refinements
- **`AdminPostingsPage.tsx`**:
  - Validated attachment upload logic using `api/admin.ts`.
  - Ensured correct use of `queryClient` for cache invalidation.
- **`AdminUsersPage.tsx`**:
  - Confirmed admin user creation flow with domain validation.
- **`AdminMailPage.tsx`**:
  - Standardized mail job creation to use the updated API function.

## 4. General Code Quality
- **`App.tsx`**:
  - Cleaned up routing configuration.
  - Added route for `UserDocumentsPage`.
  - Fixed import errors.
- **`api/applications.ts`**:
  - Deleted this redundant file, as its functionality was effectively moved to `api/user.ts` (`createSubmission`).

## 5. Next Steps
- Run the full application locally (`npm run dev`) to verify all flows (Register, Login, Profile Update, Admin functions).
- Check the `README.md` Manual Test Checklist ensuring all boxes can be checked off.
