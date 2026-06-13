import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './auth/AuthContext';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { ProtectedRoute, AdminRoute } from './routes/RouteGuards';
import { Toaster } from '@/components/ui/toaster';
import { ThemeProvider } from '@/theme/ThemeProvider';
import ThemeToggle from '@/components/ThemeToggle';

// Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import VerifyPage from './pages/VerifyPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

import PublicLayout from './components/PublicLayout';
import LandingPage from './pages/public/LandingPage';
import PostingDetailPage from './pages/public/PostingDetailPage';


import UserLayout from './components/UserLayout';
import UserHomePage from './pages/user/UserHomePage';
import UserProfilePage from './pages/user/UserProfilePage';
import UserApplicationsPage from './pages/user/UserSubmissionsPage';
import UserDocumentsPage from './pages/user/UserDocumentsPage';
import UserTasksPage from './pages/user/UserTasksPage';
import UserQuestionsPage from './pages/user/UserQuestionsPage';

import AdminLayout from './components/AdminLayout';
import AdminSubmissionsPage from './pages/admin/AdminSubmissionsPage';
import AdminApplicationDetailPage from './pages/admin/AdminApplicationDetailPage'; // Assuming this stays for detail view
import AdminPostingsPage from './pages/admin/AdminPostingsPage';
import AdminMailPage from './pages/admin/AdminMailPage';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminProfilePage from './pages/admin/AdminProfilePage';
import AdminPostingDetailPage from './pages/admin/AdminPostingDetailPage';
import AdminQuestionsPage from './pages/admin/AdminQuestionsPage';
import AdminProjectTimelinePage from './pages/admin/AdminProjectTimelinePage';
import AdminUserTimelinePage from './pages/admin/AdminUserTimelinePage';


import './index.css';

const queryClient = new QueryClient();

const App: React.FC = () => {
  return (
    <QueryClientProvider client={queryClient}>
      <ErrorBoundary>
        <ThemeProvider>
          <AuthProvider>
            <BrowserRouter>
              <Routes>
              {/* Public Routes */}
              <Route element={<PublicLayout />}>
                <Route path="/" element={<LandingPage />} />
                <Route path="/postings/:id" element={<PostingDetailPage />} />

                <Route path="/verify" element={<VerifyPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
              </Route>

              {/* Auth Routes - standalone or inside PublicLayout? Standalone usually clearer for Login */}
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />

              {/* User Routes */}
              <Route path="/user" element={<ProtectedRoute />}>
                <Route element={<UserLayout />}>
                  <Route path="home" element={<UserHomePage />} />
                  <Route path="profile" element={<UserProfilePage />} />
                  <Route path="documents" element={<UserDocumentsPage />} />
                  <Route path="tasks" element={<UserTasksPage />} />
                  <Route path="applications" element={<UserApplicationsPage />} />
                  <Route path="questions" element={<UserQuestionsPage />} />
                  <Route index element={<Navigate to="home" replace />} />
                </Route>
              </Route>

              {/* Admin Routes */}
              <Route path="/admin" element={<AdminRoute />}>
                <Route element={<AdminLayout />}>
                  <Route path="overview" element={<Navigate to="/admin/postings" replace />} />
                  <Route path="postings" element={<AdminPostingsPage />} />
                  <Route path="postings/:id/manage" element={<AdminPostingDetailPage />} />
                  <Route path="submissions" element={<AdminSubmissionsPage />} />

                  <Route path="submissions/:id" element={<AdminApplicationDetailPage />} />
                  <Route path="mail" element={<AdminMailPage />} />
                  <Route path="questions" element={<AdminQuestionsPage />} />
                  <Route path="tasks" element={<Navigate to="/admin/postings" replace />} />
                  <Route path="projects/:id/timeline" element={<AdminProjectTimelinePage />} />
                  <Route path="users/:id/timeline" element={<AdminUserTimelinePage />} />
                  <Route path="admin-users" element={<AdminUsersPage />} />
                  <Route path="profile" element={<AdminProfilePage />} />
                  <Route index element={<Navigate to="postings" replace />} />
                </Route>
              </Route>

              <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
              <ThemeToggle floating />
            </BrowserRouter>
            <Toaster />
          </AuthProvider>
        </ThemeProvider>
      </ErrorBoundary>
    </QueryClientProvider>
  );
};

export default App;
