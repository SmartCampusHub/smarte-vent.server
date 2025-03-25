-- Insert into `account` table (5 rows)
INSERT INTO `activity_management`.`account` (
  `id`, `full_name`, `password`, `email`, `role`, `inaugural_year`, `degree`, `created_at`, `updated_at`
) VALUES
  (UUID(), 'John Doe', 'hashed_pwd_123', 'john.doe@example.com', 'STUDENT', NULL, NULL, '2024-09-01 10:00:00', '2024-09-01 10:00:00'),
  (UUID(), 'Jane Smith', 'hashed_pwd_456', 'jane.smith@example.com', 'LECTURER', 2018, 'PhD', '2024-10-01 12:00:00', '2024-10-01 12:00:00'),
  (UUID(), 'Admin User', 'hashed_pwd_789', 'admin@example.com', 'ADMIN', NULL, NULL, '2024-11-01 09:00:00', '2024-11-01 09:00:00'),
  (UUID(), 'Alice Brown', 'hashed_pwd_101', 'alice.brown@example.com', 'STUDENT', NULL, NULL, '2025-01-01 14:00:00', '2025-01-01 14:00:00'),
  (UUID(), 'Bob Wilson', 'hashed_pwd_102', 'bob.wilson@example.com', 'LECTURER', 2020, 'MSc', '2025-02-01 15:00:00', '2025-02-01 15:00:00');

-- Insert into `class` table (5 rows)
INSERT INTO `activity_management`.`class` (
  `id`, `class_name`, `academic_year`, `start_date`, `end_date`, `department`, `capacity`, `status`
) VALUES
  (UUID(), 'CS101', 2024, '2024-09-01', '2025-06-30', 'Computer Science', 50, 'ACTIVE'),
  (UUID(), 'ENG201', 2025, '2025-01-01', '2025-12-31', 'Engineering', 40, 'ACTIVE'),
  (UUID(), 'MATH301', 2024, '2024-09-01', '2025-06-30', 'Mathematics', 30, 'ACTIVE'),
  (UUID(), 'PHY101', 2025, '2025-01-01', '2025-12-31', 'Physics', 45, 'ACTIVE'),
  (UUID(), 'CS202', 2023, '2023-09-01', '2024-06-30', 'Computer Science', 35, 'INACTIVE');

-- Insert into `student_account` table (5 rows)
INSERT INTO `activity_management`.`student_account` (
  `id`, `account_id`, `class_id`, `student_code`
) VALUES
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'john.doe@example.com'), (SELECT `id` FROM `class` WHERE `class_name` = 'CS101'), 'STU001'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'alice.brown@example.com'), (SELECT `id` FROM `class` WHERE `class_name` = 'ENG201'), 'STU002'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'john.doe@example.com'), (SELECT `id` FROM `class` WHERE `class_name` = 'MATH301'), 'STU003'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'alice.brown@example.com'), (SELECT `id` FROM `class` WHERE `class_name` = 'PHY101'), 'STU004'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'john.doe@example.com'), (SELECT `id` FROM `class` WHERE `class_name` = 'CS202'), 'STU005');

-- Insert into `event_category` table (5 rows)
INSERT INTO `activity_management`.`event_category` (
  `id`, `category_name`
) VALUES
  (UUID(), 'Workshop'),
  (UUID(), 'Seminar'),
  (UUID(), 'Competition'),
  (UUID(), 'Guest Lecture'),
  (UUID(), 'Social Event');

-- Insert into `representative_organizer` table (5 rows)
INSERT INTO `activity_management`.`representative_organizer` (
  `id`, `organization_name`, `representative_name`, `representative_phone`
) VALUES
  (UUID(), 'Tech Club', 'Tom Lee', '123-456-7890'),
  (UUID(), 'Engineering Society', 'Sara Kim', '234-567-8901'),
  (UUID(), 'Math Association', 'Mike Tan', '345-678-9012'),
  (UUID(), 'Physics Group', 'Emma Wong', '456-789-0123'),
  (UUID(), 'Student Union', 'Liam Chen', '567-890-1234');

-- Insert into `activity` table (5 rows)
INSERT INTO `activity_management`.`activity` (
  `id`, `attendance_score_unit`, `activity_name`, `description`, `representative_organizer_id`, `event_category_id`, `start_date`, `end_date`, `activity_venue`, `capacity`, `activity_status`, `created_at`, `updated_at`
) VALUES
  (UUID(), '5', 'AI Workshop', 'Intro to AI', (SELECT `id` FROM `representative_organizer` WHERE `organization_name` = 'Tech Club'), (SELECT `id` FROM `event_category` WHERE `category_name` = 'Workshop'), '2025-04-01 09:00:00', '2025-04-01 17:00:00', 'Room 101', 30, 'WAITING_TO_START', '2025-03-01 10:00:00', '2025-03-01 10:00:00'),
  (UUID(), '3', 'Engineering Seminar', 'Future Tech', (SELECT `id` FROM `representative_organizer` WHERE `organization_name` = 'Engineering Society'), (SELECT `id` FROM `event_category` WHERE `category_name` = 'Seminar'), '2025-03-20 14:00:00', '2025-03-20 16:00:00', 'Hall A', 50, 'ONGOING', '2025-03-15 12:00:00', '2025-03-25 08:00:00'),
  (UUID(), '10', 'Math Competition', 'Annual Math Challenge', (SELECT `id` FROM `representative_organizer` WHERE `organization_name` = 'Math Association'), (SELECT `id` FROM `event_category` WHERE `category_name` = 'Competition'), '2024-12-10 10:00:00', '2024-12-10 15:00:00', 'Room 202', 20, 'FINISHED', '2024-11-01 09:00:00', '2024-12-11 09:00:00'),
  (UUID(), '4', 'Physics Lecture', 'Quantum Mechanics', (SELECT `id` FROM `representative_organizer` WHERE `organization_name` = 'Physics Group'), (SELECT `id` FROM `event_category` WHERE `category_name` = 'Guest Lecture'), '2025-05-01 13:00:00', '2025-05-01 15:00:00', 'Lab B', 25, 'WAITING_TO_START', '2025-03-20 14:00:00', '2025-03-20 14:00:00'),
  (UUID(), '2', 'Spring Social', 'Networking Event', (SELECT `id` FROM `representative_organizer` WHERE `organization_name` = 'Student Union'), (SELECT `id` FROM `event_category` WHERE `category_name` = 'Social Event'), '2025-03-30 18:00:00', '2025-03-30 21:00:00', 'Cafeteria', 100, 'WAITING_TO_START', '2025-03-25 10:00:00', '2025-03-25 10:00:00');

-- Insert into `notification` table (5 rows)
INSERT INTO `activity_management`.`notification` (
  `id`, `account_id`, `notification_type`, `posted_by_account_id`, `message`, `created_at`
) VALUES
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'john.doe@example.com'), 'EVENT', (SELECT `id` FROM `account` WHERE `email` = 'admin@example.com'), 'AI Workshop tomorrow!', '2025-03-31 08:00:00'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'alice.brown@example.com'), 'LEARNING', (SELECT `id` FROM `account` WHERE `email` = 'jane.smith@example.com'), 'Assignment due soon', '2025-03-20 09:00:00'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'john.doe@example.com'), 'SECURITY', (SELECT `id` FROM `account` WHERE `email` = 'admin@example.com'), 'Campus lockdown drill', '2025-03-25 10:00:00'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'alice.brown@example.com'), 'EVENT', (SELECT `id` FROM `account` WHERE `email` = 'bob.wilson@example.com'), 'Seminar rescheduled', '2025-03-18 14:00:00'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'jane.smith@example.com'), 'LEARNING', (SELECT `id` FROM `account` WHERE `email` = 'admin@example.com'), 'New course materials', '2025-03-22 11:00:00');

-- Insert into `participation_detail` table (5 rows)
INSERT INTO `activity_management`.`participation_detail` (
  `id`, `account_id`, `activity_id`, `status`, `participation_role`, `qr_code`, `registered_at`
) VALUES
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'john.doe@example.com'), (SELECT `id` FROM `activity` WHERE `activity_name` = 'AI Workshop'), 'UNVERIFIED', 'PARTICIPANT', 'QR123', '2025-03-25 09:00:00'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'alice.brown@example.com'), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Engineering Seminar'), 'VERIFIED', 'PARTICIPANT', 'QR456', '2025-03-20 13:00:00'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'jane.smith@example.com'), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Math Competition'), 'VERIFIED', 'CONTRIBUTOR', NULL, '2024-12-01 08:00:00'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'bob.wilson@example.com'), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Physics Lecture'), 'UNVERIFIED', 'CONTRIBUTOR', 'QR789', '2025-03-25 14:00:00'),
  (UUID(), (SELECT `id` FROM `account` WHERE `email` = 'alice.brown@example.com'), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Spring Social'), 'UNVERIFIED', 'PARTICIPANT', 'QR101', '2025-03-25 15:00:00');

-- Insert into `confirmation` table (5 rows)
INSERT INTO `activity_management`.`confirmation` (
  `id`, `participation_id`, `rating`, `feedback_description`, `confirmed_at`, `confirmed_by_account_id`, `feedback_created_at`
) VALUES
  (UUID(), (SELECT `id` FROM `participation_detail` WHERE `qr_code` = 'QR123'), NULL, NULL, NULL, NULL, NULL),
  (UUID(), (SELECT `id` FROM `participation_detail` WHERE `qr_code` = 'QR456'), 4.50, 'Great seminar!', '2025-03-20 16:30:00', (SELECT `id` FROM `account` WHERE `email` = 'admin@example.com'), '2025-03-20 17:00:00'),
  (UUID(), (SELECT `id` FROM `participation_detail` WHERE `activity_id` = (SELECT `id` FROM `activity` WHERE `activity_name` = 'Math Competition')), 5.00, 'Well organized', '2024-12-10 16:00:00', (SELECT `id` FROM `account` WHERE `email` = 'admin@example.com'), '2024-12-11 09:00:00'),
  (UUID(), (SELECT `id` FROM `participation_detail` WHERE `qr_code` = 'QR789'), NULL, NULL, NULL, NULL, NULL),
  (UUID(), (SELECT `id` FROM `participation_detail` WHERE `qr_code` = 'QR101'), NULL, NULL, NULL, NULL, NULL);

-- Insert into `report` table (5 rows)
INSERT INTO `activity_management`.`report` (
  `id`, `activity_id`, `reported_by_account_id`, `description`, `created_at`
) VALUES
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'AI Workshop'), (SELECT `id` FROM `account` WHERE `email` = 'john.doe@example.com'), 'Room too small', '2025-03-25 11:00:00'),
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Engineering Seminar'), (SELECT `id` FROM `account` WHERE `email` = 'alice.brown@example.com'), 'Audio issues', '2025-03-20 15:00:00'),
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Math Competition'), (SELECT `id` FROM `account` WHERE `email` = 'jane.smith@example.com'), 'Late start', '2024-12-10 11:00:00'),
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Physics Lecture'), (SELECT `id` FROM `account` WHERE `email` = 'bob.wilson@example.com'), 'Projector failed', '2025-03-25 13:00:00'),
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Spring Social'), (SELECT `id` FROM `account` WHERE `email` = 'alice.brown@example.com'), 'Food shortage', '2025-03-25 16:00:00');

-- Insert into `event_schedule` table (5 rows)
INSERT INTO `activity_management`.`event_schedule` (
  `id`, `activity_id`, `start_time`, `end_time`, `activity_description`, `status`, `location`
) VALUES
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'AI Workshop'), '2025-04-01 09:00:00', '2025-04-01 11:00:00', 'Opening Session', 'WAITING_TO_START', 'Room 101'),
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Engineering Seminar'), '2025-03-20 14:00:00', '2025-03-20 15:00:00', 'Keynote Speech', 'FINISHED', 'Hall A'),
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Math Competition'), '2024-12-10 10:00:00', '2024-12-10 12:00:00', 'Round 1', 'FINISHED', 'Room 202'),
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Physics Lecture'), '2025-05-01 13:00:00', '2025-05-01 14:00:00', 'Theory Intro', 'WAITING_TO_START', 'Lab B'),
  (UUID(), (SELECT `id` FROM `activity` WHERE `activity_name` = 'Spring Social'), '2025-03-30 18:00:00', '2025-03-30 19:00:00', 'Welcome Drinks', 'WAITING_TO_START', 'Cafeteria');

-- Insert into `lecturer_manager` table (5 rows)
INSERT INTO `activity_management`.`lecturer_manager` (
  `id`, `started_date`, `class_id`, `lecturer_account_id`
) VALUES
  (UUID(), '2024-09-01', (SELECT `id` FROM `class` WHERE `class_name` = 'CS101'), (SELECT `id` FROM `account` WHERE `email` = 'jane.smith@example.com')),
  (UUID(), '2025-01-01', (SELECT `id` FROM `class` WHERE `class_name` = 'ENG201'), (SELECT `id` FROM `account` WHERE `email` = 'bob.wilson@example.com')),
  (UUID(), '2024-09-01', (SELECT `id` FROM `class` WHERE `class_name` = 'MATH301'), (SELECT `id` FROM `account` WHERE `email` = 'jane.smith@example.com')),
  (UUID(), '2025-01-01', (SELECT `id` FROM `class` WHERE `class_name` = 'PHY101'), (SELECT `id` FROM `account` WHERE `email` = 'bob.wilson@example.com')),
  (UUID(), '2023-09-01', (SELECT `id` FROM `class` WHERE `class_name` = 'CS202'), (SELECT `id` FROM `account` WHERE `email` = 'jane.smith@example.com'));

-- Insert into `student_semester_detail` table (5 rows)
INSERT INTO `activity_management`.`student_semester_detail` (
  `id`, `student_account_id`, `attendance_score`, `gpa`
) VALUES
  (UUID(), (SELECT `id` FROM `student_account` WHERE `student_code` = 'STU001'), 90.50, 3.85),
  (UUID(), (SELECT `id` FROM `student_account` WHERE `student_code` = 'STU002'), 85.00, 3.60),
  (UUID(), (SELECT `id` FROM `student_account` WHERE `student_code` = 'STU003'), 92.75, 3.90),
  (UUID(), (SELECT `id` FROM `student_account` WHERE `student_code` = 'STU004'), 88.25, 3.75),
  (UUID(), (SELECT `id` FROM `student_account` WHERE `student_code` = 'STU005'), 87.00, 3.50);