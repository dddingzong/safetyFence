-- Only connect if DB exists, otherwise create first
\set ON_ERROR_STOP on
DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'safetyfence_db') THEN
      PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE safetyfence_db');
   END IF;
END
$$;

\connect safetyfence_db;
CREATE EXTENSION IF NOT EXISTS postgis;
