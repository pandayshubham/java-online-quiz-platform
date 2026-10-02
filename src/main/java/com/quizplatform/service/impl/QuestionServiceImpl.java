package com.quizplatform.service.impl;

import com.quizplatform.dao.OptionDAO;
import com.quizplatform.dao.QuestionDAO;
import com.quizplatform.dao.impl.OptionDAOImpl;
import com.quizplatform.dao.impl.QuestionDAOImpl;
import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import com.quizplatform.service.QuestionService;
import com.quizplatform.util.ValidationUtil;
import java.util.List;

import com.quizplatform.dao.QuizDAO;
import com.quizplatform.dao.impl.QuizDAOImpl;
import com.quizplatform.model.Quiz;
import com.quizplatform.model.User;
import com.quizplatform.model.UserRole;
import com.quizplatform.util.SessionManager;

/**
 * Implementation of QuestionService.
 */
public class QuestionServiceImpl implements QuestionService {

    private final QuestionDAO questionDAO;
    private final OptionDAO optionDAO;
    private final QuizDAO quizDAO;

    public QuestionServiceImpl() {
        this(new QuestionDAOImpl(), new OptionDAOImpl(), new QuizDAOImpl());
    }

    public QuestionServiceImpl(QuestionDAO questionDAO, OptionDAO optionDAO) {
        this(questionDAO, optionDAO, new QuizDAOImpl());
    }

    public QuestionServiceImpl(QuestionDAO questionDAO, OptionDAO optionDAO, QuizDAO quizDAO) {
        this.questionDAO = questionDAO;
        this.optionDAO = optionDAO;
        this.quizDAO = quizDAO;
    }

    @Override
    public Question getQuestionById(int id) {
        return questionDAO.findById(id);
    }

    @Override
    public List<Question> getQuestionsByQuizId(int quizId) {
        return questionDAO.findByQuizId(quizId);
    }

    @Override
    public List<Option> getOptionsByQuestionId(int questionId) {
        return optionDAO.findByQuestionId(questionId);
    }

    @Override
    public Option getCorrectOptionByQuestionId(int questionId) {
        return optionDAO.findCorrectOptionByQuestionId(questionId);
    }

    @Override
    public Question createQuestion(Question question, List<Option> options) {
        validateQuestionAndOptions(question, options);

        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == UserRole.PARTICIPANT) {
                    throw new SecurityException("Access Denied: Participants cannot create questions.");
                } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                    Quiz quiz = quizDAO.findById(question.getQuizId());
                    if (quiz != null && quiz.getCreatorId() != currentUser.getId()) {
                        throw new SecurityException("Access Denied: You do not own the quiz for this question.");
                    }
                }
            }
        }

        if (question.getQuestionOrder() <= 0) {
            question.setQuestionOrder(getNextQuestionOrder(question.getQuizId()));
        }

        int questionId = questionDAO.createQuestion(question);
        question.setId(questionId);

        for (Option opt : options) {
            opt.setQuestionId(questionId);
            optionDAO.createOption(opt);
        }

        return question;
    }

    @Override
    public boolean updateQuestion(Question question, List<Option> options) {
        validateQuestionAndOptions(question, options);

        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == UserRole.PARTICIPANT) {
                    throw new SecurityException("Access Denied: Participants cannot modify questions.");
                } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                    Quiz quiz = quizDAO.findById(question.getQuizId());
                    if (quiz != null && quiz.getCreatorId() != currentUser.getId()) {
                        throw new SecurityException("Access Denied: You do not own the quiz for this question.");
                    }
                }
            }
        }

        boolean updated = questionDAO.updateQuestion(question);
        if (!updated) {
            return false;
        }

        // Replace options with the updated list
        optionDAO.deleteByQuestionId(question.getId());
        for (Option opt : options) {
            opt.setQuestionId(question.getId());
            optionDAO.createOption(opt);
        }

        return true;
    }

    @Override
    public boolean deleteQuestion(int questionId) {
        if (SessionManager.isLoggedIn()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                if (currentUser.getRole() == UserRole.PARTICIPANT) {
                    throw new SecurityException("Access Denied: Participants cannot delete questions.");
                } else if (currentUser.getRole() == UserRole.QUIZ_CREATOR) {
                    Question q = questionDAO.findById(questionId);
                    if (q != null) {
                        Quiz quiz = quizDAO.findById(q.getQuizId());
                        if (quiz != null && quiz.getCreatorId() != currentUser.getId()) {
                            throw new SecurityException("Access Denied: You do not own the quiz for this question.");
                        }
                    }
                }
            }
        }
        return questionDAO.deleteQuestion(questionId);
    }

    @Override
    public int getQuestionCountByQuizId(int quizId) {
        return questionDAO.getQuestionCountByQuizId(quizId);
    }

    @Override
    public int getNextQuestionOrder(int quizId) {
        return questionDAO.getMaxQuestionOrderByQuizId(quizId) + 1;
    }

    private void validateQuestionAndOptions(Question question, List<Option> options) {
        if (question == null) {
            throw new ValidationException("Question cannot be null.");
        }
        if (ValidationUtil.isNullOrBlank(question.getQuestionText())) {
            throw new ValidationException("Question text cannot be empty.");
        }
        if (options == null || options.size() != 4) {
            throw new ValidationException("A question must have exactly 4 options (A, B, C, D).");
        }

        int correctCount = 0;
        for (Option opt : options) {
            if (opt == null || ValidationUtil.isNullOrBlank(opt.getOptionText())) {
                throw new ValidationException("All 4 option texts must be filled.");
            }
            if (ValidationUtil.isNullOrBlank(opt.getOptionLabel())) {
                throw new ValidationException("All options must have a label (A, B, C, or D).");
            }
            if (opt.isCorrect()) {
                correctCount++;
            }
        }

        if (correctCount != 1) {
            throw new ValidationException("Exactly one option must be marked as correct.");
        }
    }
}
