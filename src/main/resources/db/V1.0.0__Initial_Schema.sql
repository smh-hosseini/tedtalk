-- Tedtaks Table
CREATE TABLE ted_talks (
       id BIGSERIAL PRIMARY KEY,
       title VARCHAR(500) NOT NULL,
       speaker VARCHAR(255) NOT NULL,
       date DATE,
       views BIGINT NOT NULL DEFAULT 0,
       likes BIGINT NOT NULL DEFAULT 0,
       link VARCHAR(500),
       CONSTRAINT unique_ted_talk UNIQUE (title, speaker, date)
);

CREATE INDEX idx_speaker ON ted_talks(speaker);
CREATE INDEX idx_year ON ted_talks(EXTRACT(YEAR FROM date));

-- Import Job Table
CREATE TABLE import_jobs (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_processed_line INT NOT NULL DEFAULT 0,
    successful_count INT NOT NULL DEFAULT 0,
    processed_count INT NOT NULL DEFAULT 0,
    failed_count INT NOT NULL DEFAULT 0,
    skipped_count INT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster lookups by file hash
CREATE UNIQUE INDEX idx_import_job_file_hash ON import_jobs(file_hash);
CREATE INDEX idx_import_job_file_name ON import_jobs(file_name);
