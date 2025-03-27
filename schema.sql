-- Disable checks for schema creation
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- Create schema
CREATE SCHEMA IF NOT EXISTS `activity_management` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `activity_management`;

-- Account table (central user entity with roles)
CREATE TABLE IF NOT EXISTS `activity_management`.`account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT, -- Changed to BIGINT with auto-increment
  `full_name` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL, -- Hashed passwords
  `email` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(15) DEFAULT NULL,
  `refresh_token` MEDIUMTEXT DEFAULT NULL,
  `role` ENUM('STUDENT', 'LECTURER', 'ADMIN') NOT NULL, -- Distinguishes account types
  `student_code` VARCHAR(20) DEFAULT NULL, -- For student accounts
  `inaugural_year` YEAR DEFAULT NULL, -- Lecturer-specific, nullable
  `degree` VARCHAR(50) DEFAULT NULL, -- Lecturer-specific, nullable
  `is_active` BOOLEAN DEFAULT TRUE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC),
  UNIQUE INDEX `student_code_UNIQUE` (`student_code` ASC)
) ENGINE = InnoDB;

-- Class table (reintroduced for class management)
CREATE TABLE IF NOT EXISTS `activity_management`.`class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `class_name` VARCHAR(100) NOT NULL,
  `academic_year` YEAR NOT NULL, -- e.g., 2023, 2024
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `department` VARCHAR(50) DEFAULT NULL, -- Optional, e.g., "Computer Science"
  `capacity` INT UNSIGNED DEFAULT NULL, -- Max students allowed
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `class_name_year_unique` (`class_name`, `academic_year` ASC) -- Prevent duplicate class names within a year
) ENGINE = InnoDB;

-- Student class enrollment (replaces student_account table)
CREATE TABLE IF NOT EXISTS `activity_management`.`student_class_enrollment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `class_id` BIGINT NOT NULL,
  `enrolled_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `fk_student_class_enrollment_account_idx` (`account_id` ASC),
  INDEX `fk_student_class_enrollment_class_idx` (`class_id` ASC),
  CONSTRAINT `fk_student_class_enrollment_account`
    FOREIGN KEY (`account_id`)
    REFERENCES `activity_management`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_student_class_enrollment_class`
    FOREIGN KEY (`class_id`)
    REFERENCES `activity_management`.`class` (`id`)
    ON DELETE RESTRICT -- Prevent deletion if students are enrolled
) ENGINE = InnoDB;

-- Event category table
CREATE TABLE IF NOT EXISTS `activity_management`.`activity_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `category_name` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `category_name_UNIQUE` (`category_name` ASC)
) ENGINE = InnoDB;

-- Representative organizer
CREATE TABLE IF NOT EXISTS `activity_management`.`representative_organizer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `organization_name` VARCHAR(100) NOT NULL,
  `representative_name` VARCHAR(100) NOT NULL,
  `representative_phone` VARCHAR(15) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB;

-- Activity table (core event entity)
CREATE TABLE IF NOT EXISTS `activity_management`.`activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `attendance_score_unit` VARCHAR(45) NOT NULL,
  `activity_name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `representative_organizer_id` BIGINT NOT NULL,
  `activity_category_id` BIGINT DEFAULT NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  `activity_venue` VARCHAR(100) NOT NULL,
  `capacity` INT UNSIGNED DEFAULT NULL,
  `activity_status` ENUM('ONGOING', 'FINISHED', 'WAITING_TO_START') NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `fk_activity_representative_organizer_idx` (`representative_organizer_id` ASC),
  INDEX `fk_activity_activity_category_idx` (`activity_category_id` ASC),
  CONSTRAINT `fk_activity_representative_organizer`
    FOREIGN KEY (`representative_organizer_id`)
    REFERENCES `activity_management`.`representative_organizer` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_activity_activity_category`
    FOREIGN KEY (`activity_category_id`)
    REFERENCES `activity_management`.`activity_category` (`id`)
    ON DELETE SET NULL
) ENGINE = InnoDB;

-- Notification table
CREATE TABLE IF NOT EXISTS `activity_management`.`notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `notification_type` ENUM('EVENT', 'LEARNING', 'SECURITY') NOT NULL,
  `posted_by_account_id` BIGINT NOT NULL,
  `message` TEXT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `fk_notification_account_idx` (`account_id` ASC),
  INDEX `fk_notification_posted_by_idx` (`posted_by_account_id` ASC),
  CONSTRAINT `fk_notification_account`
    FOREIGN KEY (`account_id`)
    REFERENCES `activity_management`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_notification_posted_by`
    FOREIGN KEY (`posted_by_account_id`)
    REFERENCES `activity_management`.`account` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Participation detail (attendance, role tracking, and feedback merged)
CREATE TABLE IF NOT EXISTS `activity_management`.`participation_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `activity_id` BIGINT NOT NULL,
  `status` ENUM('UNVERIFIED', 'VERIFIED') NOT NULL,
  `participation_role` ENUM('PARTICIPANT', 'CONTRIBUTOR') NOT NULL,
  `qr_code` VARCHAR(100) DEFAULT NULL,
  `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_participation_account_activity` (`account_id`, `activity_id`),
  CONSTRAINT `fk_participation_account`
    FOREIGN KEY (`account_id`)
    REFERENCES `activity_management`.`account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_participation_activity`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity_management`.`activity` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Confirmation table (new table for participation confirmation and feedback)
CREATE TABLE IF NOT EXISTS `activity_management`.`confirmation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `participation_id` BIGINT NOT NULL,
  `rating` DECIMAL(5,2) DEFAULT NULL,
  `feedback_description` TEXT DEFAULT NULL,
  `confirmed_at` TIMESTAMP DEFAULT NULL,
  `confirmed_by_account_id` BIGINT DEFAULT NULL,
  `feedback_created_at` TIMESTAMP DEFAULT NULL, -- Optional timestamp for feedback
  INDEX `fk_confirmation_participation_idx` (`participation_id` ASC),
  INDEX `fk_confirmation_confirmed_by_idx` (`confirmed_by_account_id` ASC),
  CONSTRAINT `fk_confirmation_participation`
    FOREIGN KEY (`participation_id`)
    REFERENCES `activity_management`.`participation_detail` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_confirmation_confirmed_by`
    FOREIGN KEY (`confirmed_by_account_id`)
    REFERENCES `activity_management`.`account` (`id`)
    ON DELETE SET NULL
) ENGINE = InnoDB;

-- Report table
CREATE TABLE IF NOT EXISTS `activity_management`.`report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `reported_by_account_id` BIGINT NOT NULL,
  `description` TEXT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `fk_report_activity_idx` (`activity_id` ASC),
  INDEX `fk_report_reported_by_idx` (`reported_by_account_id` ASC),
  CONSTRAINT `fk_report_activity`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity_management`.`activity` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_report_reported_by`
    FOREIGN KEY (`reported_by_account_id`)
    REFERENCES `activity_management`.`account` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Event schedule (detailed timeline within an activity)
CREATE TABLE IF NOT EXISTS `activity_management`.`event_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `activity_description` VARCHAR(100) NOT NULL,
  `status` ENUM('ONGOING', 'FINISHED', 'WAITING_TO_START') NOT NULL,
  `location` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_event_schedule_activity_idx` (`activity_id` ASC),
  CONSTRAINT `fk_event_schedule_activity`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity_management`.`activity` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Lecturer manager (class oversight by lecturers, references class table)
CREATE TABLE IF NOT EXISTS `activity_management`.`lecturer_manager` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `started_date` DATE NOT NULL,
  `class_id` BIGINT NOT NULL,
  `lecturer_account_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_lecturer_manager_class_idx` (`class_id` ASC),
  INDEX `fk_lecturer_manager_lecturer_idx` (`lecturer_account_id` ASC),
  CONSTRAINT `fk_lecturer_manager_class`
    FOREIGN KEY (`class_id`)
    REFERENCES `activity_management`.`class` (`id`)
    ON DELETE RESTRICT, -- Prevent deletion if managed by lecturer
  CONSTRAINT `fk_lecturer_manager_lecturer`
    FOREIGN KEY (`lecturer_account_id`)
    REFERENCES `activity_management`.`account` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Student semester detail (student performance tracking, renamed from semester)
CREATE TABLE IF NOT EXISTS `activity_management`.`student_semester_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_account_id` BIGINT NOT NULL,
  `attendance_score` DECIMAL(5,2) NOT NULL,
  `gpa` DECIMAL(3,2) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_student_semester_detail_student_account_idx` (`student_account_id` ASC),
  CONSTRAINT `fk_student_semester_detail_student_account`
    FOREIGN KEY (`student_account_id`)
    REFERENCES `activity_management`.`student_class_enrollment` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Restore original settings
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
