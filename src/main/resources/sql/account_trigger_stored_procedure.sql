-- Stored Procedures for Account Management

-- 1. Procedure to get account statistics by role
DELIMITER //
CREATE PROCEDURE get_account_statistics_by_role()
BEGIN
    SELECT 
        role,
        COUNT(*) as total_accounts,
        SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_accounts,
        SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) as inactive_accounts,
        COUNT(DISTINCT major_type) as unique_majors
    FROM account
    GROUP BY role;
END //
DELIMITER ;

-- 2. Procedure to get student statistics by major
DELIMITER //
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
END //
DELIMITER ;

-- 3. Procedure to get account participation statistics
DELIMITER //
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
        AVG(pd_count.participation_count) as avg_participations_per_account
    FROM account a
    LEFT JOIN participation_detail pd ON a.id = pd.participant_id
    LEFT JOIN (
        SELECT participant_id, COUNT(*) as participation_count
        FROM participation_detail
        GROUP BY participant_id
    ) pd_count ON a.id = pd_count.participant_id
    GROUP BY a.role;
END //
DELIMITER ;

-- 4. Procedure to get account activity metrics
DELIMITER //
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
    LEFT JOIN participation_detail pd ON a.id = pd.participant_id
    LEFT JOIN notification n ON a.id = n.receiver_id
    LEFT JOIN report r ON a.id = r.reporter_id
    WHERE a.id = p_account_id;
END //
DELIMITER ;

-- Triggers

-- 1. Trigger to validate email format
DELIMITER //
CREATE TRIGGER before_account_insert_email
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF NEW.email NOT REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid email format';
    END IF;
END //
DELIMITER ;

-- 2. Trigger to validate phone number format
DELIMITER //
CREATE TRIGGER before_account_insert_phone
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF NEW.phone NOT REGEXP '^[0-9]{10,15}$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid phone number format';
    END IF;
END //
DELIMITER ;

-- 3. Trigger to validate identify code format
DELIMITER //
CREATE TRIGGER before_account_insert_identify
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF NEW.identify_code NOT REGEXP '^[A-Za-z0-9]{8,20}$' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid identify code format';
    END IF;
END //
DELIMITER ;

-- 4. Trigger to prevent duplicate email
DELIMITER //
CREATE TRIGGER before_account_insert_email_unique
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM account WHERE email = NEW.email) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Email already exists';
    END IF;
END //
DELIMITER ;

-- 5. Trigger to prevent duplicate identify code
DELIMITER //
CREATE TRIGGER before_account_insert_identify_unique
BEFORE INSERT ON account
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM account WHERE identify_code = NEW.identify_code) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Identify code already exists';
    END IF;
END //
DELIMITER ;

-- 6. Trigger to update account status based on participation
DELIMITER //
CREATE TRIGGER after_participation_insert
AFTER INSERT ON participation_detail
FOR EACH ROW
BEGIN
    UPDATE account a
    SET a.is_active = 1
    WHERE a.id = NEW.participant_id
    AND a.is_active = 0;
END //
DELIMITER ;

-- 7. Trigger to validate role-specific constraints
DELIMITER //
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
END //
DELIMITER ;
