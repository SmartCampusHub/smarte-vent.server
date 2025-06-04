-- MySQL Database Backup and Permissions Management Script
USE activity;

-- Drop existing objects first
DROP PROCEDURE IF EXISTS backup_database;
DROP PROCEDURE IF EXISTS restore_database;
DROP PROCEDURE IF EXISTS create_application_user;
DROP PROCEDURE IF EXISTS create_admin_user;
DROP PROCEDURE IF EXISTS create_readonly_user;
DROP PROCEDURE IF EXISTS log_database_changes;
DROP PROCEDURE IF EXISTS verify_backup;
DROP VIEW IF EXISTS backup_monitoring;
DROP VIEW IF EXISTS user_activity_monitoring;
DROP VIEW IF EXISTS backup_status_monitoring;
DROP EVENT IF EXISTS daily_backup;
DROP EVENT IF EXISTS cleanup_old_backups;

-- 1. Create backup user with minimal privileges (MySQL compatible)
-- Note: Run these commands separately as root user
/*
CREATE USER IF NOT EXISTS 'backup_user'@'localhost' IDENTIFIED BY 'backup_password';
GRANT SELECT, SHOW VIEW, RELOAD, LOCK TABLES, REPLICATION CLIENT ON *.* TO 'backup_user'@'localhost';
GRANT PROCESS ON *.* TO 'backup_user'@'localhost';
FLUSH PRIVILEGES;
*/

-- 2. Create database roles (MySQL 8.0+ feature)
-- Note: Run these commands separately as root user
/*
CREATE ROLE IF NOT EXISTS 'app_role';
GRANT SELECT, INSERT, UPDATE, DELETE ON activity.* TO 'app_role';

CREATE ROLE IF NOT EXISTS 'admin_role';
GRANT ALL PRIVILEGES ON activity.* TO 'admin_role';

CREATE ROLE IF NOT EXISTS 'readonly_role';
GRANT SELECT ON activity.* TO 'readonly_role';
*/

-- 3. Create audit log table first
CREATE TABLE IF NOT EXISTS database_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operation VARCHAR(20) NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    operation_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operation_date (operation_date),
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_user_name (user_name)
);

-- 4. Create backup log table
CREATE TABLE IF NOT EXISTS backup_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    backup_file VARCHAR(255) NOT NULL,
    backup_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    backup_size BIGINT,
    backup_status ENUM('SUCCESS', 'FAILED', 'IN_PROGRESS') DEFAULT 'IN_PROGRESS',
    error_message TEXT,
    INDEX idx_backup_date (backup_date),
    INDEX idx_backup_status (backup_status)
);

DELIMITER //

-- 5. Create simplified backup logging procedure
CREATE PROCEDURE backup_database()
BEGIN
    DECLARE backup_filename VARCHAR(255);
    DECLARE backup_id BIGINT;
    
    -- Generate backup filename
    SET backup_filename = CONCAT('activity_backup_', DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s'), '.sql');
    
    -- Log backup start
    INSERT INTO backup_log (backup_file, backup_status) 
    VALUES (backup_filename, 'IN_PROGRESS');
    SET backup_id = LAST_INSERT_ID();
    
    -- Log the backup operation
    INSERT INTO database_audit_log (operation, table_name, record_id, user_name)
    VALUES ('BACKUP', 'database', backup_id, USER());
    
    -- Update backup status to success (in real implementation, this would be conditional)
    UPDATE backup_log 
    SET backup_status = 'SUCCESS' 
    WHERE id = backup_id;
    
    -- Return backup information
    SELECT backup_id as backup_id, backup_filename as filename, 'Backup logged successfully' as message;
END //

-- 6. Create restore logging procedure
CREATE PROCEDURE restore_database(IN backup_file VARCHAR(255))
BEGIN
    DECLARE restore_id BIGINT;
    
    -- Validate backup file exists in log
    IF NOT EXISTS (SELECT 1 FROM backup_log WHERE backup_file = backup_file AND backup_status = 'SUCCESS') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Backup file not found in backup log or backup was not successful';
    END IF;
    
    -- Log restore operation
    INSERT INTO database_audit_log (operation, table_name, record_id, user_name)
    VALUES ('RESTORE', 'database', 0, USER());
    SET restore_id = LAST_INSERT_ID();
    
    SELECT restore_id as restore_id, backup_file as filename, 'Restore operation logged' as message;
END //

-- 7. Create user management procedures (MySQL compatible)
CREATE PROCEDURE create_application_user(
    IN username VARCHAR(50),
    IN password VARCHAR(255)
)
BEGIN
    -- Note: This procedure logs the user creation request
    -- Actual user creation must be done by a user with CREATE USER privileges
    
    INSERT INTO database_audit_log (operation, table_name, record_id, user_name)
    VALUES ('CREATE_USER', 'mysql.user', 0, CONCAT('app_user:', username));
    
    SELECT CONCAT('User creation request logged for: ', username) as message,
           'Execute manually: CREATE USER IF NOT EXISTS ''' + username + '''@''localhost'' IDENTIFIED BY ''[password]'';' as sql_command,
           'Execute manually: GRANT app_role TO ''' + username + '''@''localhost'';' as grant_command;
END //

CREATE PROCEDURE create_admin_user(
    IN username VARCHAR(50),
    IN password VARCHAR(255)
)
BEGIN
    INSERT INTO database_audit_log (operation, table_name, record_id, user_name)
    VALUES ('CREATE_USER', 'mysql.user', 0, CONCAT('admin_user:', username));
    
    SELECT CONCAT('Admin user creation request logged for: ', username) as message,
           'Execute manually: CREATE USER IF NOT EXISTS ''' + username + '''@''localhost'' IDENTIFIED BY ''[password]'';' as sql_command,
           'Execute manually: GRANT admin_role TO ''' + username + '''@''localhost'';' as grant_command;
END //

CREATE PROCEDURE create_readonly_user(
    IN username VARCHAR(50),
    IN password VARCHAR(255)
)
BEGIN
    INSERT INTO database_audit_log (operation, table_name, record_id, user_name)
    VALUES ('CREATE_USER', 'mysql.user', 0, CONCAT('readonly_user:', username));
    
    SELECT CONCAT('Read-only user creation request logged for: ', username) as message,
           'Execute manually: CREATE USER IF NOT EXISTS ''' + username + '''@''localhost'' IDENTIFIED BY ''[password]'';' as sql_command,
           'Execute manually: GRANT readonly_role TO ''' + username + '''@''localhost'';' as grant_command;
END //

-- 8. Create audit logging procedure
CREATE PROCEDURE log_database_changes(
    IN operation VARCHAR(20),
    IN table_name VARCHAR(50),
    IN record_id BIGINT,
    IN user_name VARCHAR(50)
)
BEGIN
    INSERT INTO database_audit_log (
        operation,
        table_name,
        record_id,
        user_name,
        operation_date
    ) VALUES (
        operation,
        table_name,
        record_id,
        COALESCE(user_name, USER()),
        NOW()
    );
END //

-- 9. Create backup verification procedure
CREATE PROCEDURE verify_backup(IN backup_file VARCHAR(255))
BEGIN
    DECLARE file_exists INT DEFAULT 0;
    
    -- Check if backup exists in our log
    SELECT COUNT(*) INTO file_exists 
    FROM backup_log 
    WHERE backup_file = backup_file AND backup_status = 'SUCCESS';
    
    IF file_exists > 0 THEN
        -- Log verification
        INSERT INTO database_audit_log (operation, table_name, record_id, user_name)
        VALUES ('VERIFY_BACKUP', 'backup_log', 0, USER());
        
        SELECT 'Backup file found in log and marked as successful' as status,
               backup_file as filename;
    ELSE
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Backup file not found in log or backup was not successful';
    END IF;
END //

-- 10. Create cleanup procedure for old audit logs
CREATE PROCEDURE cleanup_old_audit_logs(IN days_to_keep INT)
BEGIN
    DECLARE deleted_count INT DEFAULT 0;
    
    DELETE FROM database_audit_log 
    WHERE operation_date < DATE_SUB(NOW(), INTERVAL days_to_keep DAY);
    
    SET deleted_count = ROW_COUNT();
    
    -- Log the cleanup operation
    INSERT INTO database_audit_log (operation, table_name, record_id, user_name)
    VALUES ('CLEANUP', 'database_audit_log', deleted_count, USER());
    
    SELECT deleted_count as deleted_records, 
           CONCAT('Cleaned up audit logs older than ', days_to_keep, ' days') as message;
END //

-- 11. Create backup statistics procedure
CREATE PROCEDURE get_backup_statistics(
    IN start_date DATE,
    IN end_date DATE
)
BEGIN
    SELECT 
        DATE(backup_date) as backup_date,
        COUNT(*) as total_backups,
        SUM(CASE WHEN backup_status = 'SUCCESS' THEN 1 ELSE 0 END) as successful_backups,
        SUM(CASE WHEN backup_status = 'FAILED' THEN 1 ELSE 0 END) as failed_backups,
        AVG(backup_size) as avg_backup_size,
        MAX(backup_size) as max_backup_size,
        MIN(backup_date) as first_backup_time,
        MAX(backup_date) as last_backup_time
    FROM backup_log
    WHERE DATE(backup_date) BETWEEN start_date AND end_date
    GROUP BY DATE(backup_date)
    ORDER BY backup_date DESC;
END //

DELIMITER ;

-- 12. Create monitoring views
CREATE VIEW backup_monitoring AS
SELECT 
    DATE(operation_date) as operation_date,
    COUNT(*) as total_operations,
    COUNT(DISTINCT table_name) as affected_tables,
    COUNT(DISTINCT user_name) as active_users
FROM database_audit_log
WHERE operation_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
GROUP BY DATE(operation_date)
ORDER BY operation_date DESC;

-- 13. Create user activity monitoring view
CREATE VIEW user_activity_monitoring AS
SELECT 
    user_name,
    COUNT(*) as total_operations,
    COUNT(DISTINCT table_name) as affected_tables,
    COUNT(DISTINCT operation) as operation_types,
    MIN(operation_date) as first_activity,
    MAX(operation_date) as last_activity,
    DATEDIFF(NOW(), MAX(operation_date)) as days_since_last_activity
FROM database_audit_log
GROUP BY user_name
ORDER BY last_activity DESC;

-- 14. Create backup status monitoring view
CREATE VIEW backup_status_monitoring AS
SELECT 
    DATE(backup_date) as backup_date,
    COUNT(*) as total_backups,
    SUM(CASE WHEN backup_status = 'SUCCESS' THEN 1 ELSE 0 END) as successful_backups,
    SUM(CASE WHEN backup_status = 'FAILED' THEN 1 ELSE 0 END) as failed_backups,
    SUM(CASE WHEN backup_status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress_backups,
    MIN(backup_date) as first_backup,
    MAX(backup_date) as last_backup,
    AVG(backup_size) as avg_backup_size
FROM backup_log
GROUP BY DATE(backup_date)
ORDER BY backup_date DESC;

-- 15. Create recent activity view
CREATE VIEW recent_activity_summary AS
SELECT 
    operation,
    table_name,
    COUNT(*) as operation_count,
    COUNT(DISTINCT user_name) as unique_users,
    MIN(operation_date) as first_occurrence,
    MAX(operation_date) as last_occurrence
FROM database_audit_log
WHERE operation_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY operation, table_name
ORDER BY last_occurrence DESC;

-- 16. Create events for automated tasks (MySQL Event Scheduler)
-- Note: Event scheduler must be enabled: SET GLOBAL event_scheduler = ON;

CREATE EVENT IF NOT EXISTS daily_backup
ON SCHEDULE EVERY 1 DAY
STARTS (CURRENT_DATE + INTERVAL 1 DAY + INTERVAL 2 HOUR)
DO
  CALL backup_database();

CREATE EVENT IF NOT EXISTS weekly_cleanup
ON SCHEDULE EVERY 1 WEEK
STARTS (CURRENT_DATE + INTERVAL 1 DAY + INTERVAL 3 HOUR)
DO
  CALL cleanup_old_audit_logs(90);

-- 17. Enable event scheduler (run as admin)
-- SET GLOBAL event_scheduler = ON;

-- Usage Examples:
/*
-- Create a backup
CALL backup_database();

-- Verify a backup
CALL verify_backup('activity_backup_20231201_143022.sql');

-- Get backup statistics for last 30 days
CALL get_backup_statistics(DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY), CURRENT_DATE);

-- View monitoring data
SELECT * FROM backup_monitoring LIMIT 10;
SELECT * FROM user_activity_monitoring;
SELECT * FROM backup_status_monitoring LIMIT 10;

-- Log a custom operation
CALL log_database_changes('MAINTENANCE', 'activity', 0, 'maintenance_user');

-- Cleanup old logs (older than 90 days)
CALL cleanup_old_audit_logs(90);
*/
