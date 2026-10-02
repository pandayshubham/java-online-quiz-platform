package com.quizplatform.service.impl;

import com.quizplatform.dao.QuestionDAO;
import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.impl.QuestionDAOImpl;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizStatus;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.QuizService;
import com.quizplatform.util.SessionManager;
import com.quizplatform.util.ValidationUtil;
import java.util.List;

/**
 * Implementation of QuizService.
 */
public class QuizServiceImpl implements QuizService {

    private final QuizDAO quizDAO;
    private final QuestionDAO questionDAO;

    public QuizServiceImpl() {
        this(new QuizDAOImpl(), new QuestionDAOImpl());
    }

    public QuizServiceImpl(QuizDAO quizDAO) {
        this(quizDAO, new QuestionDAOImpl());
    }

    public QuizServiceImpl(QuizDAO quizDAO, QuestionDAO questionDAO) {
        this.quizDAO = quizDAO;
        this.questionDAO = questionDAO;
    }

    @Override
    public Quiz getQuizById(int id) {
        return quizDAO.findById(id);
    }

    @Override
    public List<Quiz> getApprovedQuizzes() {
        return quizDAO.findApprovedQuizzes();
    }

    @Override
    public List<Quiz> getPendingQuizzes() {
        return quizDAO.findPendingApproval();
    }

    @Override
    public List<Quiz> getQuizzesByCreator(int creatorId) {
        return quizDAO.findByCreatorId(creatorId);
    }

    @Override
    public int createQuiz(Quiz quiz) {
        validateQuiz(quiz);
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == UserRole.PARTICIPANT) {
                    throw new SecurityException("Access Denied: Participants cannot create quizzes.");
                } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR && quiz.getCreatorId() != currentUser.getId()) {
                    throw new SecurityException("Access Denied: You cannot create quizzes for another user.");
                }
            }
        }
        if (quiz.getStatus() == null) {
            quiz.setStatus(QuizStatus.DRAFT);
        }
        return quizDAO.createQuiz(quiz);
    }

    @Override
    public boolean updateQuiz(Quiz quiz) {
        validateQuiz(quiz);
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == UserRole.PARTICIPANT) {
                    throw new SecurityException("Access Denied: Participants cannot modify quizzes.");
                } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                    Quiz existing = quizDAO.findById(quiz.getId());
                    if (existing != null && existing.getCreatorId() != currentUser.getId()) {
                        throw new SecurityException("Access Denied: You do not own this quiz.");
                    }
                }
            }
        }
        return quizDAO.updateQuiz(quiz);
    }

    @Override
    public boolean deleteQuiz(int quizId) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == UserRole.PARTICIPANT) {
                    throw new SecurityException("Access Denied: Participants cannot delete quizzes.");
                } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                    Quiz existing = quizDAO.findById(quizId);
                    if (existing != null && existing.getCreatorId() != currentUser.getId()) {
                        throw new SecurityException("Access Denied: You do not own this quiz.");
                    }
                }
            }
        }
        return quizDAO.deleteQuiz(quizId);
    }

    @Override
    public boolean submitQuizForApproval(int quizId) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == UserRole.PARTICIPANT) {
                    throw new SecurityException("Access Denied: Participants cannot submit quizzes.");
                } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                    Quiz existing = quizDAO.findById(quizId);
                    if (existing != null && existing.getCreatorId() != currentUser.getId()) {
                        throw new SecurityException("Access Denied: You do not own this quiz.");
                    }
                }
            }
        }
        int questionCount = questionDAO.getQuestionCountByQuizId(quizId);
        if (questionCount == 0) {
            throw new ValidationException("Quiz must contain at least one question before submitting for approval.");
        }
        return quizDAO.updateStatus(quizId, QuizStatus.PENDING_APPROVAL);
    }

    @Override
    public boolean updateQuizStatus(int quizId, QuizStatus status) {
        if (status == QuizStatus.APPROVED || status == QuizStatus.REJECTED) {
            if (SessionManager.isLoggedIn()) {
                User currentUser = SessionManager.getCurrentUser();
                if (currentUser != null && currentUser.getRole() != UserRole.ADMIN) {
                    throw new SecurityException("Access Denied: Only administrators can approve or reject quizzes.");
                }
            }
        }
        boolean updated = quizDAO.updateStatus(quizId, status);
        if (updated) {
            try {
                Quiz quiz = quizDAO.findById(quizId);
                if (quiz != null) {
                    com.quizplatform.service.NotificationService notifService = new NotificationServiceImpl();
                    notifService.sendNotification(
                            quiz.getCreatorId(),
                            "📋 Quiz " + status,
                            "Your quiz '" + quiz.getTitle() + "' status changed to " + status + "."
                    );
                }
            } catch (Exception ignored) {
                // Non-blocking notification dispatch
            }
        }
        return updated;
    }

    @Override
    public int getQuizCountByCreator(int creatorId) {
        return quizDAO.getQuizCountByCreator(creatorId);
    }

    @Override
    public int getQuizCountByCreatorAndStatus(int creatorId, QuizStatus status) {
        return quizDAO.getQuizCountByCreatorAndStatus(creatorId, status);
    }

    @Override
    public int getQuestionCountByQuizId(int quizId) {
        return quizDAO.getQuestionCountByQuizId(quizId);
    }

    @Override
    public int getTotalQuestionsCountByCreator(int creatorId) {
        return quizDAO.getTotalQuestionsCountByCreator(creatorId);
    }

    @Override
    public int getTotalAttemptsCountByCreator(int creatorId) {
        return quizDAO.getTotalAttemptsCountByCreator(creatorId);
    }

    private void validateQuiz(Quiz quiz) {
        if (quiz == null) {
            throw new ValidationException("Quiz cannot be null.");
        }
        if (ValidationUtil.isNullOrBlank(quiz.getTitle())) {
            throw new ValidationException("Quiz title cannot be empty.");
        }
        if (quiz.getDurationMinutes() <= 0) {
            throw new ValidationException("Quiz duration must be greater than 0 minutes.");
        }
    }
}

