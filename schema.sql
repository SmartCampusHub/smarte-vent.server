-- Disable checks for schema creation
SET @old_unique_checks = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @old_foreign_key_checks = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @old_sql_mode = @@SQL_MODE, SQL_MODE = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- Create schema
CREATE SCHEMA IF NOT EXISTS `activity_management` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `activity_management`;

-- Account table (central user entity with roles)
CREATE TABLE IF NOT EXISTS `account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `full_name` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(15) DEFAULT NULL,
  `refresh_token` MEDIUMTEXT DEFAULT NULL,
  `role` ENUM('STUDENT', 'LECTURER', 'ADMIN') NOT NULL,
  `student_code` VARCHAR(20) DEFAULT NULL,
  `inaugural_year` YEAR DEFAULT NULL,
  `degree` VARCHAR(50) DEFAULT NULL,
  `is_active` BOOLEAN DEFAULT TRUE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_account_email` (`email` ASC),
  UNIQUE INDEX `uk_account_student_code` (`student_code` ASC)
) ENGINE = InnoDB;

-- Class table (for class management)
CREATE TABLE IF NOT EXISTS `class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `academic_year` YEAR NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `department` VARCHAR(50) DEFAULT NULL,
  `capacity` INT UNSIGNED DEFAULT NULL,
  `status` ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_class_name_academic_year` (`name`, `academic_year` ASC)
) ENGINE = InnoDB;

-- Student class assignment (handles student-class relationships)
CREATE TABLE IF NOT EXISTS `student_class_assignment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `class_id` BIGINT NOT NULL,
  `assigned_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_assignment_account` (`account_id` ASC),
  INDEX `idx_assignment_class` (`class_id` ASC),
  CONSTRAINT `fk_assignment_account`
    FOREIGN KEY (`account_id`)
    REFERENCES `account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_assignment_class`
    FOREIGN KEY (`class_id`)
    REFERENCES `class` (`id`)
    ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Lecturer class management (tracks lecturer class management per year)
CREATE TABLE IF NOT EXISTS `lecturer_class_management` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `lecturer_account_id` BIGINT NOT NULL,
  `class_id` BIGINT NOT NULL,
  `academic_year` YEAR NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_management_lecturer` (`lecturer_account_id` ASC),
  INDEX `idx_management_class` (`class_id` ASC),
  CONSTRAINT `fk_management_lecturer`
    FOREIGN KEY (`lecturer_account_id`)
    REFERENCES `account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_management_class`
    FOREIGN KEY (`class_id`)
    REFERENCES `class` (`id`)
    ON DELETE RESTRICT
) ENGINE = InnoDB;

-- Activity category table
CREATE TABLE IF NOT EXISTS `activity_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_activity_category_name` (`name` ASC)
) ENGINE = InnoDB;

-- Representative organizer table
CREATE TABLE IF NOT EXISTS `representative_organizer` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `organization_name` VARCHAR(100) NOT NULL,
  `representative_name` VARCHAR(100) NOT NULL,
  `representative_phone` VARCHAR(15) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB;

-- Activity table (core event entity)
CREATE TABLE IF NOT EXISTS `activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `attendance_score_unit` VARCHAR(45) NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `representative_organizer_id` BIGINT NOT NULL,
  `activity_category_id` BIGINT DEFAULT NULL,
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  `venue` VARCHAR(100) NOT NULL,
  `capacity` INT UNSIGNED DEFAULT NULL,
  `status` ENUM('ONGOING', 'FINISHED', 'WAITING_TO_START') NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_activity_organizer` (`representative_organizer_id` ASC),
  INDEX `idx_activity_category` (`activity_category_id` ASC),
  CONSTRAINT `fk_activity_organizer`
    FOREIGN KEY (`representative_organizer_id`)
    REFERENCES `representative_organizer` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_activity_category`
    FOREIGN KEY (`activity_category_id`)
    REFERENCES `activity_category` (`id`)
    ON DELETE SET NULL
) ENGINE = InnoDB;

-- Notification table
CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `type` ENUM('EVENT', 'LEARNING', 'SECURITY') NOT NULL,
  `posted_by_account_id` BIGINT NOT NULL,
  `message` TEXT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_notification_account` (`account_id` ASC),
  INDEX `idx_notification_posted_by` (`posted_by_account_id` ASC),
  CONSTRAINT `fk_notification_account`
    FOREIGN KEY (`account_id`)
    REFERENCES `account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_notification_posted_by`
    FOREIGN KEY (`posted_by_account_id`)
    REFERENCES `account` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Participation detail table
CREATE TABLE IF NOT EXISTS `participation_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account_id` BIGINT NOT NULL,
  `activity_id` BIGINT NOT NULL,
  `status` ENUM('UNVERIFIED', 'VERIFIED') NOT NULL,
  `role` ENUM('PARTICIPANT', 'CONTRIBUTOR') NOT NULL,
  `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_participation_account_activity` (`account_id`, `activity_id` ASC),
  CONSTRAINT `fk_participation_account`
    FOREIGN KEY (`account_id`)
    REFERENCES `account` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_participation_activity`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Confirmation table
CREATE TABLE IF NOT EXISTS `confirmation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `participation_id` BIGINT NOT NULL,
  `rating` DECIMAL(5,2) DEFAULT NULL,
  `feedback_description` TEXT DEFAULT NULL,
  `confirmed_at` TIMESTAMP DEFAULT NULL,
  `confirmer_id` BIGINT DEFAULT NULL,
  `feedback_created_at` TIMESTAMP DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_confirmation_participation` (`participation_id` ASC),
  INDEX `idx_confirmation_confirmer` (`confirmer_id` ASC),
  CONSTRAINT `fk_confirmation_participation`
    FOREIGN KEY (`participation_id`)
    REFERENCES `participation_detail` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_confirmation_confirmer`
    FOREIGN KEY (`confirmer_id`)
    REFERENCES `account` (`id`)
    ON DELETE SET NULL
) ENGINE = InnoDB;

-- Report table
CREATE TABLE IF NOT EXISTS `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `reported_by_account_id` BIGINT NOT NULL,
  `description` TEXT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_report_activity` (`activity_id` ASC),
  INDEX `idx_report_reported_by` (`reported_by_account_id` ASC),
  CONSTRAINT `fk_report_activity`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_report_reported_by`
    FOREIGN KEY (`reported_by_account_id`)
    REFERENCES `account` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Event schedule table
CREATE TABLE IF NOT EXISTS `event_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `activity_id` BIGINT NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL,
  `description` VARCHAR(100) NOT NULL,
  `status` ENUM('ONGOING', 'FINISHED', 'WAITING_TO_START') NOT NULL,
  `location` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_event_schedule_activity` (`activity_id` ASC),
  CONSTRAINT `fk_event_schedule_activity`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Lecturer manager table
CREATE TABLE IF NOT EXISTS `lecturer_manager` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `start_date` DATE NOT NULL,
  `class_id` BIGINT NOT NULL,
  `lecturer_account_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_lecturer_manager_class` (`class_id` ASC),
  INDEX `idx_lecturer_manager_lecturer` (`lecturer_account_id` ASC),
  CONSTRAINT `fk_lecturer_manager_class`
    FOREIGN KEY (`class_id`)
    REFERENCES `class` (`id`)
    ON DELETE RESTRICT,
  CONSTRAINT `fk_lecturer_manager_lecturer`
    FOREIGN KEY (`lecturer_account_id`)
    REFERENCES `account` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Student semester detail table
CREATE TABLE IF NOT EXISTS `student_semester_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_assignment_id` BIGINT NOT NULL,
  `attendance_score` DECIMAL(5,2) NOT NULL,
  `gpa` DECIMAL(3,2) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_semester_detail_assignment` (`student_assignment_id` ASC),
  CONSTRAINT `fk_semester_detail_assignment`
    FOREIGN KEY (`student_assignment_id`)
    REFERENCES `student_class_assignment` (`id`)
    ON DELETE CASCADE
) ENGINE = InnoDB;

-- Restore original settings
SET SQL_MODE = @old_sql_mode;
SET FOREIGN_KEY_CHECKS = @old_foreign_key_checks;
SET UNIQUE_CHECKS = @old_unique_checks;
