package com.quizplatform.dao;

import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizStatus;
import java.util.List;

/**
 * Data Access Object interface for Quiz entities.
 */
public interface QuizDAO {

    int createQuiz(Quiz quiz);

    Quiz findById(int id);

    List<Quiz> findAll();

    List<Quiz> findByCreatorId(int creatorId);

    List<Quiz> findApprovedQuizzes();

    List<Quiz> findPendingApproval();

    List<Quiz> findByStatus(QuizStatus status);

    boolean updateQuiz(Quiz quiz);

    boolean deleteQuiz(int id);

    boolean updateStatus(int quizId, QuizStatus status);

    int getQuizCountByCreator(int creatorId);

    int getQuizCountByCreatorAndStatus(int creatorId, QuizStatus status);

    int getQuestionCountByQuizId(int quizId);

    int getTotalQuestionsCountByCreator(int creatorId);

    int getTotalAttemptsCountByCreator(int creatorId);
}

