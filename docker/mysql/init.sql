-- MySQL initialization script for Activity Management System
-- This script runs when the MySQL container starts for the first time

-- Set character set and collation
SET character_set_server = utf8mb4;
SET collation_server = utf8mb4_unicode_ci;

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS activity 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Create application user with proper permissions
CREATE USER IF NOT EXISTS 'activity_user'@'%' IDENTIFIED BY 'activity_pass';
GRANT ALL PRIVILEGES ON activity.* TO 'activity_user'@'%';

-- Grant permissions to root for external connections
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'rootpassword' WITH GRANT OPTION;

-- Flush privileges to ensure changes take effect
FLUSH PRIVILEGES;

-- Use the activity database
USE activity;

-- Create initial tables if needed (Hibernate will handle most of this)
-- But we can set some initial configuration

-- Set timezone
SET time_zone = '+00:00';

-- Performance optimizations
SET GLOBAL innodb_buffer_pool_size = 134217728; -- 128MB for development
SET GLOBAL max_connections = 200;
SET GLOBAL wait_timeout = 28800;
SET GLOBAL interactive_timeout = 28800;

-- Log initialization
SELECT 'Activity Management Database initialized successfully!' as message; 
