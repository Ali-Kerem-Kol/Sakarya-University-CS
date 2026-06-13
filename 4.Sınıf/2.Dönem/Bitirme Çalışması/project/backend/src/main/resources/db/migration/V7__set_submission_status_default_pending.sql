ALTER TABLE application_submissions
    ALTER COLUMN status SET DEFAULT 'PENDING';

UPDATE application_submissions
SET status = 'PENDING'
WHERE status IS NULL;
