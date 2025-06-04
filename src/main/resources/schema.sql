-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema activity
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema activity
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `activity` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `activity` ;

-- -----------------------------------------------------
-- Table `activity`.`account`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`account` (
  `status` BIT(1) NULL DEFAULT NULL,
  `created_date` DATETIME(6) NULL DEFAULT NULL,
  `id` BIGINT NOT NULL,
  `updated_date` DATETIME(6) NULL DEFAULT NULL,
  `refresh_token` VARCHAR(1024) NULL DEFAULT NULL,
  `created_by` VARCHAR(255) NULL DEFAULT NULL,
  `email` VARCHAR(255) NULL DEFAULT NULL,
  `full_name` VARCHAR(255) NULL DEFAULT NULL,
  `identify_code` VARCHAR(255) NULL DEFAULT NULL,
  `password` VARCHAR(255) NULL DEFAULT NULL,
  `phone` VARCHAR(255) NULL DEFAULT NULL,
  `updated_by` VARCHAR(255) NULL DEFAULT NULL,
  `major_type` ENUM('IT', 'EE', 'IS', 'AE', 'AI') NULL DEFAULT NULL,
  `role` ENUM('ADMIN', 'STUDENT', 'ORGANIZATION') NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`account_seq`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`account_seq` (
  `next_val` BIGINT NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`organization`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`organization` (
  `id` BIGINT NOT NULL,
  `updated_date` DATETIME(6) NULL DEFAULT NULL,
  `description` VARCHAR(255) NULL DEFAULT NULL,
  `email` VARCHAR(255) NULL DEFAULT NULL,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  `phone` VARCHAR(255) NULL DEFAULT NULL,
  `type` ENUM('CLUB', 'COMPANY', 'UNIVERSITY', 'GOVERNMENT') NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK5tcxdwx9wg89b66krbkm35d3d`
    FOREIGN KEY (`id`)
    REFERENCES `activity`.`account` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`activity`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`activity` (
  `attendance_score` INT NULL DEFAULT NULL,
  `current_participants` INT NULL DEFAULT NULL,
  `fee` DOUBLE NULL DEFAULT NULL,
  `is_approved` BIT(1) NULL DEFAULT NULL,
  `is_featured` BIT(1) NULL DEFAULT NULL,
  `latitude` DOUBLE NULL DEFAULT NULL,
  `likes` INT NULL DEFAULT NULL,
  `longitude` DOUBLE NULL DEFAULT NULL,
  `max_attendees` INT NULL DEFAULT NULL,
  `created_by_id` BIGINT NULL DEFAULT NULL,
  `created_date` DATETIME(6) NULL DEFAULT NULL,
  `end_date` DATETIME(6) NULL DEFAULT NULL,
  `id` BIGINT NOT NULL,
  `organization_id` BIGINT NULL DEFAULT NULL,
  `registration_deadline` DATETIME(6) NULL DEFAULT NULL,
  `start_date` DATETIME(6) NULL DEFAULT NULL,
  `updated_by_id` BIGINT NULL DEFAULT NULL,
  `updated_date` DATETIME(6) NULL DEFAULT NULL,
  `version` BIGINT NULL DEFAULT NULL,
  `address` VARCHAR(255) NULL DEFAULT NULL,
  `description` TEXT NULL DEFAULT NULL,
  `image_url` VARCHAR(255) NULL DEFAULT NULL,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  `online_link` VARCHAR(255) NULL DEFAULT NULL,
  `short_description` TEXT NULL DEFAULT NULL,
  `venue` VARCHAR(255) NULL DEFAULT NULL,
  `category` ENUM('STUDENT_ORGANIZATION', 'UNIVERSITY', 'THIRD_PARTY') NULL DEFAULT NULL,
  `status` ENUM('IN_PROGRESS', 'COMPLETED', 'PUBLISHED', 'CANCELLED', 'PENDING') NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKej14sg7lr18s5ppva579i2p9p` (`organization_id` ASC) VISIBLE,
  CONSTRAINT `FKej14sg7lr18s5ppva579i2p9p`
    FOREIGN KEY (`organization_id`)
    REFERENCES `activity`.`organization` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`attendance`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`attendance` (
  `activity_id` BIGINT NULL DEFAULT NULL,
  `attendee_id` BIGINT NULL DEFAULT NULL,
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `registered_at` DATETIME(6) NULL DEFAULT NULL,
  `created_by` VARCHAR(255) NULL DEFAULT NULL,
  `aattendance_status` ENUM('UNVERIFIED', 'VERIFIED', 'REJECTED') NULL DEFAULT NULL,
  `attendee_role` ENUM('PARTICIPANT', 'CONTRIBUTOR') NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKey1f232tapbr4hrd90blf6ga6` (`activity_id` ASC) VISIBLE,
  INDEX `FKnnvn617cx83rwgtpqa52i6m02` (`attendee_id` ASC) VISIBLE,
  CONSTRAINT `FKey1f232tapbr4hrd90blf6ga6`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity`.`activity` (`id`),
  CONSTRAINT `FKnnvn617cx83rwgtpqa52i6m02`
    FOREIGN KEY (`attendee_id`)
    REFERENCES `activity`.`account` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 307
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`eactivity_tags`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`eactivity_tags` (
  `eactivity_id` BIGINT NOT NULL,
  `tags` VARCHAR(255) NULL DEFAULT NULL,
  INDEX `FK9qpphxaxnwkyi1qlpyxywlav5` (`eactivity_id` ASC) VISIBLE,
  CONSTRAINT `FK9qpphxaxnwkyi1qlpyxywlav5`
    FOREIGN KEY (`eactivity_id`)
    REFERENCES `activity`.`activity` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`event_schedule`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`event_schedule` (
  `activity_id` BIGINT NULL DEFAULT NULL,
  `created_date` DATETIME(6) NULL DEFAULT NULL,
  `end_time` DATETIME(6) NULL DEFAULT NULL,
  `id` BIGINT NOT NULL,
  `start_time` DATETIME(6) NULL DEFAULT NULL,
  `updated_date` DATETIME(6) NULL DEFAULT NULL,
  `activity_description` VARCHAR(255) NULL DEFAULT NULL,
  `created_by` VARCHAR(255) NULL DEFAULT NULL,
  `location` VARCHAR(255) NULL DEFAULT NULL,
  `updated_by` VARCHAR(255) NULL DEFAULT NULL,
  `status` ENUM('IN_PROGRESS', 'COMPLETED', 'WAITING_TO_START', 'CANCELLED') NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKf6qcnrvi9jhofb7w4rn0x1n86` (`activity_id` ASC) VISIBLE,
  CONSTRAINT `FKf6qcnrvi9jhofb7w4rn0x1n86`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity`.`activity` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`event_schedule_seq`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`event_schedule_seq` (
  `next_val` BIGINT NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`feedback`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`feedback` (
  `rating` DOUBLE NULL DEFAULT NULL,
  `activity_id` BIGINT NULL DEFAULT NULL,
  `id` BIGINT NOT NULL,
  `pariticipation_id` BIGINT NULL DEFAULT NULL,
  `feedback_description` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKm1ckvh3wperfq36bxvc7xqha6` (`activity_id` ASC) VISIBLE,
  INDEX `FKbtx4u3nwjeqi60lea4j25t5ch` (`pariticipation_id` ASC) VISIBLE,
  CONSTRAINT `FKbtx4u3nwjeqi60lea4j25t5ch`
    FOREIGN KEY (`pariticipation_id`)
    REFERENCES `activity`.`attendance` (`id`),
  CONSTRAINT `FKm1ckvh3wperfq36bxvc7xqha6`
    FOREIGN KEY (`activity_id`)
    REFERENCES `activity`.`activity` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`feedback_seq`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`feedback_seq` (
  `next_val` BIGINT NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`notification`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`notification` (
  `is_read` BIT(1) NULL DEFAULT NULL,
  `created_date` DATETIME(6) NULL DEFAULT NULL,
  `id` BIGINT NOT NULL,
  `receiver_id` BIGINT NULL DEFAULT NULL,
  `sender_id` BIGINT NULL DEFAULT NULL,
  `content` TEXT NULL DEFAULT NULL,
  `title` VARCHAR(255) NULL DEFAULT NULL,
  `notification_type` ENUM('ACTIVITY', 'LEARNING', 'SECURITY') NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FKcfvggnpv0ky0elvkbnhyslqic` (`receiver_id` ASC) VISIBLE,
  INDEX `FKtnwqi152dhvkvlm7kc58pe63p` (`sender_id` ASC) VISIBLE,
  CONSTRAINT `FKcfvggnpv0ky0elvkbnhyslqic`
    FOREIGN KEY (`receiver_id`)
    REFERENCES `activity`.`account` (`id`),
  CONSTRAINT `FKtnwqi152dhvkvlm7kc58pe63p`
    FOREIGN KEY (`sender_id`)
    REFERENCES `activity`.`account` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`notification_seq`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`notification_seq` (
  `next_val` BIGINT NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`report`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`report` (
  `report_type` TINYINT NULL DEFAULT NULL,
  `created_date` DATETIME(6) NULL DEFAULT NULL,
  `id` BIGINT NOT NULL,
  `reported_object_id` BIGINT NULL DEFAULT NULL,
  `reporter_id` BIGINT NULL DEFAULT NULL,
  `created_by` VARCHAR(255) NULL DEFAULT NULL,
  `description` TEXT NULL DEFAULT NULL,
  `title` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `FK44irattr7g97gw7lfttxudql7` (`reporter_id` ASC) VISIBLE,
  CONSTRAINT `FK44irattr7g97gw7lfttxudql7`
    FOREIGN KEY (`reporter_id`)
    REFERENCES `activity`.`account` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `activity`.`report_seq`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `activity`.`report_seq` (
  `next_val` BIGINT NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

















