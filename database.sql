-- ====================================================================
-- Database Schema for Java Online Quiz Platform
-- Database Name: quiz_platform
-- ====================================================================

-- 1. Create Database if not exists
CREATE DATABASE IF NOT EXISTS quiz_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE quiz_platform;

-- ====================================================================
-- Table 1: users
-- Supports ADMIN, QUIZ_CREATOR, and PARTICIPANT roles
-- ====================================================================
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'QUIZ_CREATOR', 'PARTICIPANT') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_role (role),
    INDEX idx_users_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 2: quizzes
-- Quizzes created by QUIZ_CREATOR, approved by ADMIN
-- ====================================================================
CREATE TABLE IF NOT EXISTS quizzes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    creator_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL DEFAULT 15,
    status ENUM('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_quizzes_creator FOREIGN KEY (creator_id) 
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_quizzes_creator (creator_id),
    INDEX idx_quizzes_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 3: questions
-- Questions associated with quizzes
-- ====================================================================
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    question_text TEXT NOT NULL,
    explanation TEXT,
    question_order INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) 
        REFERENCES quizzes(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_questions_quiz (quiz_id),
    INDEX idx_questions_order (quiz_id, question_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 4: options
-- Four options (A, B, C, D) per question
-- ====================================================================
CREATE TABLE IF NOT EXISTS options (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL,
    option_text TEXT NOT NULL,
    option_label CHAR(1) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_options_question FOREIGN KEY (question_id) 
        REFERENCES questions(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_question_option_label UNIQUE (question_id, option_label),
    INDEX idx_options_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 5: quiz_attempts
-- Historical record of quiz attempts by participants
-- Uses RESTRICT to protect historical attempt records from deletion
-- ====================================================================
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id INT NOT NULL,
    participant_id INT NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    score INT NOT NULL DEFAULT 0,
    total_questions INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    incorrect_answers INT NOT NULL DEFAULT 0,
    unanswered_questions INT NOT NULL DEFAULT 0,
    percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    status ENUM('IN_PROGRESS', 'COMPLETED', 'TIME_EXPIRED') NOT NULL DEFAULT 'IN_PROGRESS',
    CONSTRAINT fk_attempts_quiz FOREIGN KEY (quiz_id) 
        REFERENCES quizzes(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_attempts_participant FOREIGN KEY (participant_id) 
        REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_attempts_quiz (quiz_id),
    INDEX idx_attempts_participant (participant_id),
    INDEX idx_attempts_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 6: answers
-- Answers submitted by participants during an attempt
-- ====================================================================
CREATE TABLE IF NOT EXISTS answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option_id INT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_answers_attempt FOREIGN KEY (attempt_id) 
        REFERENCES quiz_attempts(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_answers_question FOREIGN KEY (question_id) 
        REFERENCES questions(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_answers_option FOREIGN KEY (selected_option_id) 
        REFERENCES options(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_answers_attempt (attempt_id),
    INDEX idx_answers_question (question_id),
    INDEX idx_answers_selected_option (selected_option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 7: messages
-- Communication between participants and quiz creators
-- ====================================================================
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    message_text TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_messages_sender (sender_id),
    INDEX idx_messages_receiver (receiver_id),
    INDEX idx_messages_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 8: notifications
-- System alerts and notifications for users
-- ====================================================================
CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 9: system_settings
-- Global platform configuration settings
-- ====================================================================
CREATE TABLE IF NOT EXISTS system_settings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_settings_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- Table 10: quiz_reminders
-- Participant scheduled reminders for upcoming quizzes
-- ====================================================================
CREATE TABLE IF NOT EXISTS quiz_reminders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    participant_id INT NOT NULL,
    quiz_id INT NOT NULL,
    reminder_time DATETIME NOT NULL,
    reminder_status ENUM('PENDING', 'SENT', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminders_participant FOREIGN KEY (participant_id) 
        REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reminders_quiz FOREIGN KEY (quiz_id) 
        REFERENCES quizzes(id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_reminders_participant (participant_id),
    INDEX idx_reminders_quiz (quiz_id),
    INDEX idx_reminders_status (reminder_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
