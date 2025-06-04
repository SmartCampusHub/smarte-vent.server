-- Stored Procedures for Activity Schedule Management

-- 1. Procedure to get schedule statistics by status
DELIMITER //
CREATE PROCEDURE get_schedule_statistics_by_status()
BEGIN
    SELECT 
        status,
        COUNT(*) as total_schedules,
        COUNT(DISTINCT activity_id) as unique_activities,
        AVG(TIMESTAMPDIFF(HOUR, start_time, end_time)) as avg_duration_hours
    FROM event_schedule
    GROUP BY status;
END //
DELIMITER ;

-- 2. Procedure to get schedule conflicts
DELIMITER //
CREATE PROCEDURE check_schedule_conflicts(
    IN p_activity_id BIGINT,
    IN p_start_time TIMESTAMP,
    IN p_end_time TIMESTAMP
)
BEGIN
    SELECT 
        es.id,
        es.activity_id,
        es.start_time,
        es.end_time,
        es.location
    FROM event_schedule es
    WHERE es.activity_id != p_activity_id
    AND es.status != 'CANCELLED'
    AND (
        (p_start_time BETWEEN es.start_time AND es.end_time)
        OR (p_end_time BETWEEN es.start_time AND es.end_time)
        OR (es.start_time BETWEEN p_start_time AND p_end_time)
    );
END //
DELIMITER ;

-- 3. Procedure to get schedule statistics by time period
DELIMITER //
CREATE PROCEDURE get_schedule_statistics_by_period(
    IN p_period VARCHAR(10),
    IN p_start_date TIMESTAMP,
    IN p_end_date TIMESTAMP
)
BEGIN
    SELECT 
        DATE_FORMAT(start_time, 
            CASE p_period
                WHEN 'DAY' THEN '%Y-%m-%d'
                WHEN 'WEEK' THEN '%Y-%u'
                WHEN 'MONTH' THEN '%Y-%m'
                WHEN 'QUARTER' THEN '%Y-%q'
                WHEN 'YEAR' THEN '%Y'
                ELSE '%Y-%m-%d'
            END
        ) as period,
        COUNT(*) as total_schedules,
        COUNT(DISTINCT activity_id) as unique_activities,
        COUNT(DISTINCT CASE WHEN status = 'COMPLETED' THEN id END) as completed_schedules,
        COUNT(DISTINCT CASE WHEN status = 'CANCELLED' THEN id END) as cancelled_schedules,
        AVG(TIMESTAMPDIFF(HOUR, start_time, end_time)) as avg_duration_hours
    FROM event_schedule
    WHERE start_time BETWEEN p_start_date AND p_end_date
    GROUP BY period
    ORDER BY period;
END //
DELIMITER ;

-- Triggers

-- 1. Trigger to validate schedule times
DELIMITER //
CREATE TRIGGER before_schedule_insert_time_validation
BEFORE INSERT ON event_schedule
FOR EACH ROW
BEGIN
    IF NEW.start_time >= NEW.end_time THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'End time must be after start time';
    END IF;
END //
DELIMITER ;

-- 2. Trigger to prevent overlapping schedules for same activity
DELIMITER //
CREATE TRIGGER before_schedule_insert_overlap_check
BEFORE INSERT ON event_schedule
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM event_schedule
        WHERE activity_id = NEW.activity_id
        AND status != 'CANCELLED'
        AND (
            (NEW.start_time BETWEEN start_time AND end_time)
            OR (NEW.end_time BETWEEN start_time AND end_time)
            OR (start_time BETWEEN NEW.start_time AND NEW.end_time)
        )
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Schedule overlaps with existing schedule for this activity';
    END IF;
END //
DELIMITER ;

-- 3. Trigger to update activity status based on schedule
DELIMITER //
CREATE TRIGGER after_schedule_status_update
AFTER UPDATE ON event_schedule
FOR EACH ROW
BEGIN
    IF NEW.status != OLD.status THEN
        UPDATE activity a
        SET a.status = 
            CASE 
                WHEN EXISTS (
                    SELECT 1 FROM event_schedule 
                    WHERE activity_id = a.id 
                    AND status = 'IN_PROGRESS'
                ) THEN 'IN_PROGRESS'
                WHEN EXISTS (
                    SELECT 1 FROM event_schedule 
                    WHERE activity_id = a.id 
                    AND status = 'WAITING_TO_START'
                ) THEN 'WAITING_TO_START'
                WHEN NOT EXISTS (
                    SELECT 1 FROM event_schedule 
                    WHERE activity_id = a.id 
                    AND status IN ('IN_PROGRESS', 'WAITING_TO_START')
                ) THEN 'COMPLETED'
            END
        WHERE a.id = NEW.activity_id;
    END IF;
END //
DELIMITER ;
