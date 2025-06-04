-- Stored Procedures for Activity Management

-- 1. Procedure to update activity status based on dates
DELIMITER //
CREATE PROCEDURE update_activity_status()
BEGIN
    -- Update activities that have started but not ended
    UPDATE activity 
    SET status = 'IN_PROGRESS'
    WHERE start_date <= CURRENT_TIMESTAMP 
    AND end_date > CURRENT_TIMESTAMP
    AND status = 'PUBLISHED';

    -- Update activities that have ended
    UPDATE activity 
    SET status = 'COMPLETED'
    WHERE end_date < CURRENT_TIMESTAMP
    AND status IN ('PUBLISHED', 'IN_PROGRESS');
END //
DELIMITER ;

-- 2. Procedure to calculate activity statistics
DELIMITER //
CREATE PROCEDURE calculate_activity_statistics(
    IN p_start_date TIMESTAMP,
    IN p_end_date TIMESTAMP
)
BEGIN
    SELECT 
        a.activity_category,
        COUNT(*) as total_activities,
        SUM(a.current_participants) as total_participants,
        AVG(a.current_participants) as avg_participants,
        SUM(a.capacity_limit) as total_capacity,
        (SUM(a.current_participants) * 100.0 / SUM(a.capacity_limit)) as participation_rate
    FROM activity a
    WHERE a.start_date BETWEEN p_start_date AND p_end_date
    GROUP BY a.activity_category;
END //
DELIMITER ;

-- 3. Procedure to get organization performance metrics
DELIMITER //
CREATE PROCEDURE get_organization_metrics(
    IN p_organization_id BIGINT,
    IN p_start_date TIMESTAMP,
    IN p_end_date TIMESTAMP
)
BEGIN
    SELECT 
        COUNT(*) as total_activities,
        SUM(a.current_participants) as total_participants,
        AVG(a.current_participants) as avg_participants_per_activity,
        SUM(a.capacity_limit) as total_capacity,
        (SUM(a.current_participants) * 100.0 / SUM(a.capacity_limit)) as overall_participation_rate,
        COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_activities,
        COUNT(CASE WHEN a.status = 'IN_PROGRESS' THEN 1 END) as ongoing_activities,
        COUNT(CASE WHEN a.status = 'PUBLISHED' THEN 1 END) as upcoming_activities
    FROM activity a
    WHERE a.organization_id = p_organization_id
    AND a.start_date BETWEEN p_start_date AND p_end_date;
END //
DELIMITER ;

-- 4. Procedure to get activity trends
DELIMITER //
CREATE PROCEDURE get_activity_trends(
    IN p_start_date TIMESTAMP,
    IN p_end_date TIMESTAMP
)
BEGIN
    SELECT 
        DATE_FORMAT(start_date, '%Y-%m') as month,
        activity_category,
        COUNT(*) as activity_count,
        SUM(current_participants) as total_participants,
        AVG(current_participants) as avg_participants
    FROM activity
    WHERE start_date BETWEEN p_start_date AND p_end_date
    GROUP BY DATE_FORMAT(start_date, '%Y-%m'), activity_category
    ORDER BY month, activity_category;
END //
DELIMITER ;

-- Triggers

-- 1. Trigger to update current_participants when participation changes
DELIMITER //
CREATE TRIGGER after_participation_change
AFTER INSERT ON participation_detail
FOR EACH ROW
BEGIN
    UPDATE activity 
    SET current_participants = current_participants + 1
    WHERE id = NEW.activity_id;
END //
DELIMITER ;

-- 2. Trigger to validate activity dates
DELIMITER //
CREATE TRIGGER before_activity_insert
BEFORE INSERT ON activity
FOR EACH ROW
BEGIN
    IF NEW.start_date >= NEW.end_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Start date must be before end date';
    END IF;
    
    IF NEW.registration_deadline >= NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Registration deadline must be before start date';
    END IF;
END //
DELIMITER ;

-- 3. Trigger to update activity status on date changes
DELIMITER //
CREATE TRIGGER after_activity_date_update
AFTER UPDATE ON activity
FOR EACH ROW
BEGIN
    IF NEW.start_date != OLD.start_date OR NEW.end_date != OLD.end_date THEN
        IF NEW.start_date <= CURRENT_TIMESTAMP AND NEW.end_date > CURRENT_TIMESTAMP THEN
            SET NEW.status = 'IN_PROGRESS';
        ELSEIF NEW.end_date < CURRENT_TIMESTAMP THEN
            SET NEW.status = 'COMPLETED';
        END IF;
    END IF;
END //
DELIMITER ;

-- 4. Trigger to validate capacity limit
DELIMITER //
CREATE TRIGGER before_participation_insert
BEFORE INSERT ON participation_detail
FOR EACH ROW
BEGIN
    DECLARE current_count INT;
    DECLARE max_capacity INT;
    
    SELECT current_participants, capacity_limit 
    INTO current_count, max_capacity
    FROM activity 
    WHERE id = NEW.activity_id;
    
    IF current_count >= max_capacity THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Activity has reached maximum capacity';
    END IF;
END //
DELIMITER ;
