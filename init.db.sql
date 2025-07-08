-- Create database if not exists
SELECT 'CREATE DATABASE ziyara_db'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ziyara_db')\gexec

-- Connect to ziyara_db
    \c ziyara_db;

-- Create user if not exists
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_user
      WHERE usename = 'ziyara_user') THEN

      CREATE USER ziyara_user WITH PASSWORD 'ziyara_pass';
END IF;
END
$do$;

-- Grant all privileges
GRANT ALL PRIVILEGES ON DATABASE ziyara_db TO ziyara_user;
GRANT ALL ON SCHEMA public TO ziyara_user;