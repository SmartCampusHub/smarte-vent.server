-- Account Management Stored Procedures and Triggers
USE activity;
DELIMITER //

-- Drop all existing procedures and triggers first
DROP PROCEDURE IF EXISTS get_account_statistics_by_role//
DROP PROCEDURE IF EXISTS get_student_statistics_by_major//
DROP PROCEDURE IF EXISTS get_account_participation_stats//
DROP PROCEDURE IF EXISTS get_account_activity_metrics//

DROP TRIGGER IF EXISTS before_account_insert_email//
DROP TRIGGER IF EXISTS before_account_insert_phone//
DROP TRIGGER IF EXISTS before_account_insert_identify//
DROP TRIGGER IF EXISTS before_account_insert_email_unique//
DROP TRIGGER IF EXISTS before_account_insert_identify_unique//
DROP TRIGGER IF EXISTS before_account_insert_role//
DROP TRIGGER IF EXISTS after_participation_insert//

-- Now create all procedures and triggers

-- 1. Account Statistics Procedures
CREATE PROCEDURE get_account_statistics_by_role()
READS SQL DATA
BEGIN
    SELECT 
        role,
        COUNT(*) as total_accounts,
        SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_accounts,
        SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) as inactive_accounts,
        COUNT(DISTINCT major_type) as unique_majors
    FROM account
    GROUP BY role;
END//

CREATE PROCEDURE get_student_statistics_by_major()
BEGIN
    SELECT 
        major_type,
        COUNT(*) as total_students,
        SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_students,
        COUNT(DISTINCT identify_code) as unique_identities
    FROM account
    WHERE role = 'STUDENT'
    GROUP BY major_type;
END//

CREATE PROCEDURE get_account_participation_stats(
    IN p_start_date TIMESTAMP,
    IN p_end_date TIMESTAMP
)
BEGIN
    SELECT 
        a.role,
        COUNT(DISTINCT a.id) as total_accounts,
        COUNT(DISTINCT pd.id) as total_participations,
        COUNT(DISTINCT CASE WHEN pd.created_date BETWEEN p_start_date AND p_end_date THEN pd.id END) as new_participations,
        COALESCE(AVG(pd_count.participation_count), 0) as avg_participations_per_account
    FROM account a
    LEFT JOIN attendance pd ON a.id = pd.participant_id
    LEFT JOIN (
        SELECT participant_id, COUNT(*) as participation_count
        FROM attendance
        GROUP BY participant_id
    ) pd_count ON a.id = pd_count.participant_id
    GROUP BY a.role;
END//

CREATE PROCEDURE get_account_activity_metrics(
    IN p_account_id BIGINT
)
BEGIN
    SELECT 
        COUNT(DISTINCT pd.id) as total_participations,
        COUNT(DISTINCT n.id) as total_notifications,
        COUNT(DISTINCT r.id) as total_reports,
        COUNT(DISTINCT CASE WHEN pd.created_date >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY) THEN pd.id END) as recent_participations,
        COUNT(DISTINCT CASE WHEN n.created_date >= DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 30 DAY) THEN n.id END) as recent_notifications
    FROM account a
    LEFT JOIN attendance pd ON a.id = pd.participant_id
    LEFT JOIN notification n ON a.id = n.receiver_id
    LEFT JOIN report r ON a.id = r.reporter_id
    WHERE a.id = p_account_id;
END//

-- Account Triggers
CREATE TRIGGER before_account_insert_email
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF NEW.email NOT REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid email format';
    END IF;
END//

CREATE TRIGGER before_account_insert_phone
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF NEW.phone NOT REGEXP '^[0-9]{10,15}$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid phone number format';
    END IF;
END//

CREATE TRIGGER before_account_insert_identify
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF NEW.identify_code NOT REGEXP '^[A-Za-z0-9]{8,20}$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid identify code format';
    END IF;
END//

CREATE TRIGGER before_account_insert_email_unique
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    DECLARE email_count INT DEFAULT 0;
    SELECT COUNT(*) INTO email_count FROM account WHERE email = NEW.email;
    IF email_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Email already exists';
    END IF;
END//

CREATE TRIGGER before_account_insert_identify_unique
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    DECLARE identify_count INT DEFAULT 0;
    SELECT COUNT(*) INTO identify_count FROM account WHERE identify_code = NEW.identify_code;
    IF identify_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Identify code already exists';
    END IF;
END//

CREATE TRIGGER before_account_insert_role
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF NEW.role = 'STUDENT' AND NEW.major_type IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Student accounts must have a major type';
    END IF;
    
    IF NEW.role = 'ORGANIZATION' AND NEW.major_type IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Organization accounts cannot have a major type';
    END IF;
END//

CREATE TRIGGER after_participation_insert
AFTER INSERT ON attendance
FOR EACH ROW
BEGIN
    UPDATE account a
    SET a.is_active = 1
    WHERE a.id = NEW.participant_id
    AND a.is_active = 0;
END//

DELIMITER ;
