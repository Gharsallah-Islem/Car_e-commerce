-- ==============================================================================
-- AutoParts Store - Database Initialization Script
-- ==============================================================================
-- This script runs automatically when PostgreSQL container starts for the first time

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ecommercespareparts TO lasmer;

-- Create indexes for better performance (if tables exist)
-- These will be created by Hibernate, but we add comments for reference

-- Note: JPA/Hibernate will handle table creation via ddl-auto=update
-- This file is for additional database setup like extensions and initial data

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed successfully!';
END $$;
