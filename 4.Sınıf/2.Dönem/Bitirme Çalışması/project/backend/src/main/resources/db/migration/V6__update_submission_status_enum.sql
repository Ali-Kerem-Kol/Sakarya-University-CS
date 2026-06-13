-- Submission status migration ACCEPTED -> APPROVED
UPDATE application_submissions
SET status = 'APPROVED'
WHERE status = 'ACCEPTED';
