-- ====================================================================
-- Database Seed Data for Java Online Quiz Platform
-- Database Name: quiz_platform
-- ====================================================================

USE quiz_platform;

-- ====================================================================
-- 1. Initial System Settings
-- ====================================================================
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
    ('default_quiz_duration', '15', 'Default quiz duration in minutes'),
    ('maximum_quiz_duration', '180', 'Maximum allowable quiz duration in minutes'),
    ('maximum_attempts', '3', 'Maximum number of attempts allowed per quiz'),
    ('leaderboard_enabled', 'true', 'Global flag to enable or disable public leaderboard'),
    ('reminders_enabled', 'true', 'Global flag to enable or disable quiz reminders'),
    ('quiz_attempts_enabled', 'true', 'Global flag to enable or disable new quiz attempts'),
    ('messaging_enabled', 'true', 'Global flag to enable or disable platform messaging'),
    ('notifications_enabled', 'true', 'Global flag to enable or disable platform notifications')
ON DUPLICATE KEY UPDATE 
    setting_value = VALUES(setting_value),
    description = VALUES(description);

-- ====================================================================
-- 2. Seed Users
-- Passwords are stored in plain text for this academic/demonstration phase
-- as strictly requested (no external BCrypt/hashing libraries).
-- 
-- Test Credentials Summary:
-- Role: ADMIN
--   - Email: admin@quizplatform.com | Password: admin123
-- Role: QUIZ_CREATOR
--   - Email: creator1@quizplatform.com | Password: creator123
--   - Email: creator2@quizplatform.com | Password: creator123
-- Role: PARTICIPANT
--   - Email: student1@quizplatform.com | Password: student123
--   - Email: student2@quizplatform.com | Password: student123
--   - Email: student3@quizplatform.com | Password: student123
-- ====================================================================

-- 1 Admin User
INSERT INTO users (name, email, password, role, is_active) VALUES
    ('System Administrator', 'admin@quizplatform.com', 'admin123', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    password = VALUES(password),
    role = VALUES(role),
    is_active = VALUES(is_active);

-- 2 Quiz Creator Users
INSERT INTO users (name, email, password, role, is_active) VALUES
    ('Prof. Alice Johnson', 'creator1@quizplatform.com', 'creator123', 'QUIZ_CREATOR', TRUE),
    ('Dr. Bob Smith', 'creator2@quizplatform.com', 'creator123', 'QUIZ_CREATOR', TRUE)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    password = VALUES(password),
    role = VALUES(role),
    is_active = VALUES(is_active);

-- 3 Participant Users
INSERT INTO users (name, email, password, role, is_active) VALUES
    ('Charlie Brown', 'student1@quizplatform.com', 'student123', 'PARTICIPANT', TRUE),
    ('Diana Prince', 'student2@quizplatform.com', 'student123', 'PARTICIPANT', TRUE),
    ('Evan Wright', 'student3@quizplatform.com', 'student123', 'PARTICIPANT', TRUE)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    password = VALUES(password),
    role = VALUES(role),
    is_active = VALUES(is_active);

-- ====================================================================
-- 3. Initial Approved Seed Quizzes for Participant Testing
-- ====================================================================
INSERT INTO quizzes (id, creator_id, title, description, duration_minutes, status) VALUES
    (1, 2, 'Java Core & OOP Fundamentals', 'Test your knowledge on Object-Oriented Programming, classes, inheritance, and keywords.', 10, 'APPROVED'),
    (2, 3, 'Database Systems & SQL Basics', 'Comprehensive quiz covering relational databases, SQL queries, constraints, and joins.', 15, 'APPROVED')
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    duration_minutes = VALUES(duration_minutes),
    status = VALUES(status);

-- Questions for Quiz 1
INSERT INTO questions (id, quiz_id, question_text, explanation, question_order) VALUES
    (1, 1, 'Which OOP principle focuses on hiding internal state and requiring interaction through public methods?', 'Encapsulation bundles data and methods, preventing direct access from external code.', 1),
    (2, 1, 'Which keyword in Java prevents a class from being subclassed?', 'The final keyword on a class prevents inheritance.', 2),
    (3, 1, 'What is the default value of a boolean primitive variable when declared as an instance member in Java?', 'Primitive boolean defaults to false in Java member fields.', 3)
ON DUPLICATE KEY UPDATE
    question_text = VALUES(question_text),
    explanation = VALUES(explanation),
    question_order = VALUES(question_order);

-- Options for Question 1
INSERT INTO options (id, question_id, option_text, option_label, is_correct) VALUES
    (1, 1, 'Inheritance', 'A', FALSE),
    (2, 1, 'Polymorphism', 'B', FALSE),
    (3, 1, 'Encapsulation', 'C', TRUE),
    (4, 1, 'Abstraction', 'D', FALSE)
ON DUPLICATE KEY UPDATE option_text = VALUES(option_text), is_correct = VALUES(is_correct);

-- Options for Question 2
INSERT INTO options (id, question_id, option_text, option_label, is_correct) VALUES
    (5, 2, 'static', 'A', FALSE),
    (6, 2, 'final', 'B', TRUE),
    (7, 2, 'abstract', 'C', FALSE),
    (8, 2, 'super', 'D', FALSE)
ON DUPLICATE KEY UPDATE option_text = VALUES(option_text), is_correct = VALUES(is_correct);

-- Options for Question 3
INSERT INTO options (id, question_id, option_text, option_label, is_correct) VALUES
    (9, 3, 'true', 'A', FALSE),
    (10, 3, 'false', 'B', TRUE),
    (11, 3, 'null', 'C', FALSE),
    (12, 3, '0', 'D', FALSE)
ON DUPLICATE KEY UPDATE option_text = VALUES(option_text), is_correct = VALUES(is_correct);

-- Questions for Quiz 2
INSERT INTO questions (id, quiz_id, question_text, explanation, question_order) VALUES
    (4, 2, 'Which SQL clause is used to filter records resulting from a GROUP BY operation?', 'HAVING filters aggregated groups, whereas WHERE filters individual rows prior to grouping.', 1),
    (5, 2, 'Which constraint uniquely identifies each record in a relational database table?', 'PRIMARY KEY uniquely identifies rows and cannot contain NULL values.', 2)
ON DUPLICATE KEY UPDATE
    question_text = VALUES(question_text),
    explanation = VALUES(explanation),
    question_order = VALUES(question_order);

-- Options for Question 4
INSERT INTO options (id, question_id, option_text, option_label, is_correct) VALUES
    (13, 4, 'WHERE', 'A', FALSE),
    (14, 4, 'HAVING', 'B', TRUE),
    (15, 4, 'ORDER BY', 'C', FALSE),
    (16, 4, 'FILTER BY', 'D', FALSE)
ON DUPLICATE KEY UPDATE option_text = VALUES(option_text), is_correct = VALUES(is_correct);

-- Options for Question 5
INSERT INTO options (id, question_id, option_text, option_label, is_correct) VALUES
    (17, 5, 'FOREIGN KEY', 'A', FALSE),
    (18, 5, 'CHECK', 'B', FALSE),
    (19, 5, 'PRIMARY KEY', 'C', TRUE),
    (20, 5, 'UNIQUE', 'D', FALSE)
ON DUPLICATE KEY UPDATE option_text = VALUES(option_text), is_correct = VALUES(is_correct);
