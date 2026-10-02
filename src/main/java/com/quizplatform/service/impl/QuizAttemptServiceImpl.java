package com.quizplatform.service.impl;

import com.quizplatform.config.DatabaseConnection;
import com.quizplatform.dao.AnswerDAO;
import com.quizplatform.dao.OptionDAO;
import com.quizplatform.dao.QuestionDAO;
import com.quizplatform.dao.QuizAttemptDAO;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.impl.AnswerDAOImpl;
import com.quizplatform.dao.impl.OptionDAOImpl;
import com.quizplatform.dao.impl.QuestionDAOImpl;
import com.quizplatform.dao.impl.QuizAttemptDAOImpl;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.exception.DatabaseException;
import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.Answer;
import com.quizplatform.model.AttemptStatus;
import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.QuizAttemptService;
import com.quizplatform.util.SessionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.quizplatform.dao.SystemSettingDAO;
import com.quizplatform.dao.impl.SystemSettingDAOImpl;
import com.quizplatform.model.QuizStatus;
import com.quizplatform.service.concurrency.AttemptSubmissionCoordinator;

/**
 * Implementation of QuizAttemptService with transaction management,
 * attempt access authorization, and server-side timer validation.
 */
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizAttemptDAO attemptDAO;
    private final QuestionDAO questionDAO;
    private final OptionDAO optionDAO;
    private final AnswerDAO answerDAO;
    private final QuizDAO quizDAO;
    private final SystemSettingDAO systemSettingDAO;
    private final AttemptSubmissionCoordinator submissionCoordinator;

    public QuizAttemptServiceImpl() {
        this(new QuizAttemptDAOImpl(), new QuestionDAOImpl(), new OptionDAOImpl(), new AnswerDAOImpl(), new QuizDAOImpl(), new SystemSettingDAOImpl());
    }

    public QuizAttemptServiceImpl(QuizAttemptDAO attemptDAO) {
        this(attemptDAO, new QuestionDAOImpl(), new OptionDAOImpl(), new AnswerDAOImpl(), new QuizDAOImpl(), new SystemSettingDAOImpl());
    }

    public QuizAttemptServiceImpl(QuizAttemptDAO attemptDAO, QuestionDAO questionDAO, OptionDAO optionDAO, AnswerDAO answerDAO) {
        this(attemptDAO, questionDAO, optionDAO, answerDAO, new QuizDAOImpl(), new SystemSettingDAOImpl());
    }

    public QuizAttemptServiceImpl(QuizAttemptDAO attemptDAO, QuestionDAO questionDAO, OptionDAO optionDAO, AnswerDAO answerDAO, QuizDAO quizDAO) {
        this(attemptDAO, questionDAO, optionDAO, answerDAO, quizDAO, new SystemSettingDAOImpl());
    }

    public QuizAttemptServiceImpl(QuizAttemptDAO attemptDAO, QuestionDAO questionDAO, OptionDAO optionDAO, AnswerDAO answerDAO, QuizDAO quizDAO, SystemSettingDAO systemSettingDAO) {
        this(attemptDAO, questionDAO, optionDAO, answerDAO, quizDAO, systemSettingDAO, new AttemptSubmissionCoordinator());
    }

    public QuizAttemptServiceImpl(QuizAttemptDAO attemptDAO, QuestionDAO questionDAO, OptionDAO optionDAO, AnswerDAO answerDAO, QuizDAO quizDAO, SystemSettingDAO systemSettingDAO, AttemptSubmissionCoordinator submissionCoordinator) {
        this.attemptDAO = attemptDAO;
        this.questionDAO = questionDAO;
        this.optionDAO = optionDAO;
        this.answerDAO = answerDAO;
        this.quizDAO = quizDAO;
        this.systemSettingDAO = systemSettingDAO;
        this.submissionCoordinator = submissionCoordinator != null ? submissionCoordinator : new AttemptSubmissionCoordinator();
    }

    public AttemptSubmissionCoordinator getSubmissionCoordinator() {
        return submissionCoordinator;
    }

    public boolean isQuizAttemptsEnabled() {
        if (systemSettingDAO == null) return true;
        com.quizplatform.model.SystemSetting s = systemSettingDAO.findByKey("quiz_attempts_enabled");
        if (s == null || s.getSettingValue() == null) return true;
        return Boolean.parseBoolean(s.getSettingValue().trim());
    }

    private void checkAttemptAccess(QuizAttempt attempt) {
        if (attempt == null) return;
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == UserRole.PARTICIPANT) {
                    if (attempt.getParticipantId() != currentUser.getId()) {
                        throw new SecurityException("Access Denied: You are not authorized to access another participant's quiz attempt.");
                    }
                } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                    Quiz quiz = quizDAO.findById(attempt.getQuizId());
                    if (quiz != null && quiz.getCreatorId() != currentUser.getId()) {
                        throw new SecurityException("Access Denied: You do not own the quiz for this attempt.");
                    }
                }
            }
        }
    }

    @Override
    public QuizAttempt getAttemptById(int id) {
        QuizAttempt attempt = attemptDAO.findById(id);
        if (attempt != null) {
            checkAttemptAccess(attempt);
        }
        return attempt;
    }

    @Override
    public List<QuizAttempt> getAttemptsByParticipant(int participantId) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == UserRole.PARTICIPANT && currentUser.getId() != participantId) {
                throw new SecurityException("Access Denied: You can only view your own attempts.");
            }
        }
        return attemptDAO.findByParticipantId(participantId);
    }

    @Override
    public List<QuizAttempt> getAttemptsByQuiz(int quizId) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                Quiz quiz = quizDAO.findById(quizId);
                if (quiz != null && quiz.getCreatorId() != currentUser.getId()) {
                    throw new SecurityException("Access Denied: You do not own this quiz.");
                }
            }
        }
        return attemptDAO.findByQuizId(quizId);
    }

    @Override
    public int startAttempt(int quizId, int participantId) {
        if (!isQuizAttemptsEnabled()) {
            throw new ValidationException("Quiz attempts are currently disabled by administrator.");
        }
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() == UserRole.PARTICIPANT && currentUser.getId() != participantId) {
                throw new SecurityException("Access Denied: You cannot start an attempt for another user.");
            }
        }
        Quiz quiz = quizDAO.findById(quizId);
        if (quiz == null) {
            throw new ValidationException("Quiz not found with id: " + quizId);
        }
        if (quiz.getStatus() != QuizStatus.APPROVED) {
            throw new ValidationException("Cannot start attempt: Quiz is not approved (Current status: " + quiz.getStatus() + ").");
        }
        QuizAttempt attempt = new QuizAttempt(quizId, participantId);
        return attemptDAO.createAttempt(attempt);
    }

    @Override
    public QuizAttempt submitAttempt(int attemptId, Map<Integer, Integer> questionToOptionMap, boolean timeExpired) {
        // Demonstrates SYNCHRONIZATION via AttemptSubmissionCoordinator
        return submissionCoordinator.coordinateSubmission(attemptId, () -> {
            QuizAttempt attempt = attemptDAO.findById(attemptId);
            if (attempt == null) {
                throw new IllegalArgumentException("Quiz attempt not found with id: " + attemptId);
            }

            // Enforce authorization
            checkAttemptAccess(attempt);

            // Prevent duplicate submission: An attempt may be submitted only if status == IN_PROGRESS
            if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
                throw new ValidationException("This quiz attempt has already been submitted.");
            }

            Quiz quiz = quizDAO.findById(attempt.getQuizId());
            if (quiz == null) {
                throw new IllegalArgumentException("Quiz not found with id: " + attempt.getQuizId());
            }

            boolean finalTimeExpired = timeExpired;
            // Server-side / Database-based timer validation:
            // deadline = started_at + duration_minutes
            if (attempt.getStartedAt() != null) {
                LocalDateTime deadline = attempt.getStartedAt().plusMinutes(quiz.getDurationMinutes());
                if (LocalDateTime.now().isAfter(deadline)) {
                    finalTimeExpired = true;
                }
            }

            // Execute all submission steps inside ONE database transaction
            // Demonstrates JDBC Transactions: setAutoCommit(false), commit(), rollback()
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                List<Question> questions = questionDAO.findByQuizId(conn, quiz.getId());
                int correctCount = 0;
                int incorrectCount = 0;
                int unansweredCount = 0;

                for (Question q : questions) {
                    Integer selectedOptionId = questionToOptionMap != null ? questionToOptionMap.get(q.getId()) : null;
                    Option correctOpt = optionDAO.findCorrectOptionByQuestionId(conn, q.getId());

                    boolean isCorrect = false;
                    if (selectedOptionId == null) {
                        unansweredCount++;
                    } else if (correctOpt != null && selectedOptionId.equals(correctOpt.getId())) {
                        isCorrect = true;
                        correctCount++;
                    } else {
                        incorrectCount++;
                    }

                    Answer answer = new Answer(0, attemptId, q.getId(), selectedOptionId, isCorrect);
                    answerDAO.createAnswer(conn, answer);
                }

                attempt.setCompletedAt(LocalDateTime.now());
                attempt.setScore(correctCount);
                attempt.setTotalQuestions(questions.size());
                attempt.setCorrectAnswers(correctCount);
                attempt.setIncorrectAnswers(incorrectCount);
                attempt.setUnansweredQuestions(unansweredCount);
                double percentage = questions.size() > 0 ? (correctCount * 100.0) / questions.size() : 0.0;
                attempt.setPercentage(Math.round(percentage * 100.0) / 100.0);
                attempt.setStatus(finalTimeExpired ? AttemptStatus.TIME_EXPIRED : AttemptStatus.COMPLETED);

                attemptDAO.updateAttempt(conn, attempt);

                conn.commit();

                try {
                    Quiz q = quizDAO.findById(attempt.getQuizId());
                    String qTitle = (q != null && q.getTitle() != null) ? q.getTitle() : "Quiz";
                    com.quizplatform.service.NotificationService notifService = new NotificationServiceImpl();
                    notifService.sendNotification(
                            attempt.getParticipantId(),
                            "🎯 Quiz Completed: " + qTitle,
                            "You scored " + attempt.getScore() + "/" + attempt.getTotalQuestions() + " (" + attempt.getPercentage() + "%)."
                    );
                } catch (Exception ignored) {
                    // Non-blocking notification dispatch
                }

                return attempt;
            } catch (Exception e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        // ignore
                    }
                }
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new DatabaseException("Failed to submit quiz attempt due to transaction failure: " + e.getMessage(), e);
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException closeEx) {
                        // ignore
                    }
                }
            }
        });
    }

    @Override
    public List<Answer> getAnswersByAttempt(int attemptId) {
        QuizAttempt attempt = attemptDAO.findById(attemptId);
        if (attempt != null) {
            checkAttemptAccess(attempt);
        }
        return answerDAO.findByAttemptId(attemptId);
    }

    @Override
    public int getAttemptsCountByParticipant(int participantId) {
        return attemptDAO.getAttemptsCountByParticipant(participantId);
    }

    @Override
    public int getCompletedAttemptsCountByParticipant(int participantId) {
        return attemptDAO.getCompletedAttemptsCountByParticipant(participantId);
    }

    @Override
    public double getAveragePercentageByParticipant(int participantId) {
        return attemptDAO.getAveragePercentageByParticipant(participantId);
    }

    @Override
    public int getHighestScoreByParticipant(int participantId) {
        return attemptDAO.getHighestScoreByParticipant(participantId);
    }

    @Override
    public List<QuizAttempt> getAttemptsByCreator(int creatorId) {
        return attemptDAO.findByCreatorId(creatorId);
    }

    @Override
    public double getAverageScoreByCreator(int creatorId) {
        return attemptDAO.getAverageScoreByCreatorId(creatorId);
    }

    @Override
    public int getHighestScoreByCreator(int creatorId) {
        return attemptDAO.getHighestScoreByCreatorId(creatorId);
    }

    @Override
    public int getLowestScoreByCreator(int creatorId) {
        return attemptDAO.getLowestScoreByCreatorId(creatorId);
    }

    @Override
    public double getAverageScoreByQuiz(int quizId) {
        return attemptDAO.getAverageScoreByQuizId(quizId);
    }

    @Override
    public int getHighestScoreByQuiz(int quizId) {
        return attemptDAO.getHighestScoreByQuizId(quizId);
    }

    @Override
    public int getLowestScoreByQuiz(int quizId) {
        return attemptDAO.getLowestScoreByQuizId(quizId);
    }

    @Override
    public QuizAttempt getActiveAttempt(int quizId, int participantId) {
        return attemptDAO.findActiveAttemptByParticipantAndQuiz(participantId, quizId);
    }

    @Override
    public List<QuizAttempt> getAttemptsByParticipantAndQuiz(int participantId, int quizId) {
        return attemptDAO.findByParticipantIdAndQuizId(participantId, quizId);
    }

    @Override
    public int getTimeExpiredAttemptsCount() {
        return attemptDAO.getTimeExpiredAttemptsCount();
    }

    @Override
    public double getPlatformAverageScore() {
        return attemptDAO.getPlatformAverageScore();
    }

    @Override
    public List<QuizAttempt> getRecentAttempts(int limit) {
        return attemptDAO.findRecentAttempts(limit);
    }
}


