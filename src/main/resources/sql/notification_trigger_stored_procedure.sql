-- Stored Procedures for Notification Management

-- 1. Procedure to get notification statistics by type
DELIMITER //
CREATE PROCEDURE get_notification_statistics_by_type()
BEGIN
    SELECT 
        notification_type,
        COUNT(*) as total_notifications,
        COUNT(DISTINCT receiver_id) as unique_receivers,
        SUM(CASE WHEN is_read = 1 THEN 1 ELSE 0 END) as read_count,
        SUM(CASE WHEN is_read = 0 THEN 1 ELSE 0 END) as unread_count,
        AVG(TIMESTAMPDIFF(HOUR, created_date, NOW())) as avg_age_hours
    FROM notification
    GROUP BY notification_type;
END //
DELIMITER ;

-- 2. Procedure to get user notification metrics
DELIMITER //
CREATE PROCEDURE get_user_notification_metrics(
    IN p_user_id BIGINT,
    IN p_start_date TIMESTAMP,
    IN p_end_date TIMESTAMP
)
BEGIN
    SELECT 
        COUNT(*) as total_notifications,
        SUM(CASE WHEN is_read = 1 THEN 1 ELSE 0 END) as read_count,
        SUM(CASE WHEN is_read = 0 THEN 1 ELSE 0 END) as unread_count,
        AVG(TIMESTAMPDIFF(HOUR, created_date, NOW())) as avg_response_time_hours,
        COUNT(DISTINCT notification_type) as notification_types_received
    FROM notification
    WHERE receiver_id = p_user_id
    AND created_date BETWEEN p_start_date AND p_end_date;
END //
DELIMITER ;

-- 3. Procedure to get notification engagement metrics
DELIMITER //
CREATE PROCEDURE get_notification_engagement_metrics(
    IN p_notification_type VARCHAR(20)
)
BEGIN
    SELECT 
        DATE_FORMAT(created_date, '%Y-%m-%d') as date,
        COUNT(*) as total_sent,
        SUM(CASE WHEN is_read = 1 THEN 1 ELSE 0 END) as read_count,
        AVG(TIMESTAMPDIFF(HOUR, created_date, 
            CASE WHEN is_read = 1 THEN updated_date ELSE NOW() END
        )) as avg_read_time_hours
    FROM notification
    WHERE notification_type = p_notification_type
    GROUP BY DATE_FORMAT(created_date, '%Y-%m-%d')
    ORDER BY date DESC;
END //
DELIMITER ;

-- Triggers

-- 1. Trigger to validate notification type
DELIMITER //
CREATE TRIGGER before_notification_insert_type_validation
BEFORE INSERT ON notification
FOR EACH ROW
BEGIN
    IF NEW.notification_type NOT IN ('ACTIVITY', 'LEARNING', 'SECURITY') THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Invalid notification type';
    END IF;
END //
DELIMITER ;

-- 2. Trigger to set default read status
DELIMITER //
CREATE TRIGGER before_notification_insert_default_read
BEFORE INSERT ON notification
FOR EACH ROW
BEGIN
    IF NEW.is_read IS NULL THEN
        SET NEW.is_read = 0;
    END IF;
END //
DELIMITER ;

-- 3. Trigger to prevent duplicate notifications
DELIMITER //
CREATE TRIGGER before_notification_insert_duplicate_check
BEFORE INSERT ON notification
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM notification
        WHERE receiver_id = NEW.receiver_id
        AND notification_type = NEW.notification_type
        AND content = NEW.content
        AND created_date > DATE_SUB(NOW(), INTERVAL 1 HOUR)
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Duplicate notification detected within the last hour';
    END IF;
END //
DELIMITER ;
