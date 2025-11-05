-- Database initialization script
-- This runs automatically when PostgreSQL container starts for the first time

-- Connect to the application database
\c safetyfence_db;

-- Create PostGIS extension in the application database
CREATE EXTENSION IF NOT EXISTS postgis;

-- Verify PostGIS installation
SELECT PostGIS_version();