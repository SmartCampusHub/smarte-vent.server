-- Stored Procedures for Report Management

-- 1. Procedure to get report statistics by type
DELIMITER //
CREATE PROCEDURE get_report_statistics_by_type()
BEGIN
    SELECT 
        report_type,
        COUNT(*) as total_reports,
        COUNT(DISTINCT reporter_id) as unique_reporters,
        COUNT(DISTINCT reported_object_id) as unique_reported_objects,
        AVG(TIMESTAMPDIFF(HOUR, created_date, NOW())) as avg_age_hours
    FROM report
    GROUP BY report_type;
END //
DELIMITER ;

-- 2. Procedure to get reporter statistics
DELIMITER //
CREATE PROCEDURE get_reporter_statistics(
    IN p_start_date TIMESTAMP,
    IN p_end_date TIMESTAMP
)
BEGIN
    SELECT 
        r.reporter_id,
        COUNT(*) as total_reports,
        COUNT(DISTINCT r.report_type) as report_types_used,
        COUNT(DISTINCT r.reported_object_id) as unique_objects_reported,
        AVG(TIMESTAMPDIFF(HOUR, r.created_date, NOW())) as avg_report_age_hours
    FROM report r
    WHERE r.created_date BETWEEN p_start_date AND p_end_date
    GROUP BY r.reporter_id;
END //
DELIMITER ;

-- 3. Procedure to get reported object statistics
DELIMITER //
CREATE PROCEDURE get_reported_object_statistics(
    IN p_report_type VARCHAR(20)
)
BEGIN
    SELECT 
        reported_object_id,
        COUNT(*) as report_count,
        COUNT(DISTINCT reporter_id) as unique_reporters,
        MIN(created_date) as first_report_date,
        MAX(created_date) as last_report_date,
        AVG(TIMESTAMPDIFF(HOUR, created_date, NOW())) as avg_report_age_hours
    FROM report
    WHERE report_type = p_report_type
    GROUP BY reported_object_id
    HAVING report_count > 1
    ORDER BY report_count DESC;
END //
DELIMITER ;

-- Triggers

-- 1. Trigger to validate report type
DELIMITER //
CREATE TRIGGER before_report_insert_type_validation
BEFORE INSERT ON report
FOR EACH ROW
BEGIN
    IF NEW.report_type NOT IN ('ACTIVITY', 'USER', 'ORGANIZATION') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid report type';
    END IF;
END //
DELIMITER ;

-- 2. Trigger to prevent duplicate reports
DELIMITER //
CREATE TRIGGER before_report_insert_duplicate_check
BEFORE INSERT ON report
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM report
        WHERE reporter_id = NEW.reporter_id
        AND report_type = NEW.report_type
        AND reported_object_id = NEW.reported_object_id
        AND created_date > DATE_SUB(NOW(), INTERVAL 24 HOUR)
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Duplicate report detected within the last 24 hours';
    END IF;
END //
DELIMITER ;

-- 3. Trigger to validate reported object existence
DELIMITER //
CREATE TRIGGER before_report_insert_object_validation
BEFORE INSERT ON report
FOR EACH ROW
BEGIN
    DECLARE object_exists INT;
    
    CASE NEW.report_type
        WHEN 'ACTIVITY' THEN
            SELECT COUNT(*) INTO object_exists FROM activity WHERE id = NEW.reported_object_id;
        WHEN 'USER' THEN
            SELECT COUNT(*) INTO object_exists FROM account WHERE id = NEW.reported_object_id;
        WHEN 'ORGANIZATION' THEN
            SELECT COUNT(*) INTO object_exists FROM account WHERE id = NEW.reported_object_id AND role = 'ORGANIZATION';
    END CASE;
    
    IF object_exists = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Reported object does not exist';
    END IF;
END //
DELIMITER ;
