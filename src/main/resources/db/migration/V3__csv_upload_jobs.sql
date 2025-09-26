CREATE TABLE IF NOT EXISTS csv_upload_jobs (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    message TEXT,
    error_report TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_csv_upload_jobs_status ON csv_upload_jobs(status);
