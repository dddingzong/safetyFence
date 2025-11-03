-- Database initialization script
-- This runs automatically when PostgreSQL container starts for the first time

-- Create PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Verify PostGIS installation
SELECT PostGIS_version();
