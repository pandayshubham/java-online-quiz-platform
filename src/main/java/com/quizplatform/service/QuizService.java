package com.quizplatform.service;

import com.quizplatform.model.Quiz;
import com.quizplatform.model.QuizStatus;
import java.util.List;

/**
 * Service interface for quiz operations and approval workflow.
 */
public interface QuizService {

    Quiz getQuizById(int id);

    List<Quiz> getApprovedQuizzes();

    List<Quiz> getPendingQuizzes();

    List<Quiz> getQuizzesByCreator(int creatorId);

    int createQuiz(Quiz quiz);

    boolean updateQuiz(Quiz quiz);

    boolean deleteQuiz(int quizId);

    boolean submitQuizForApproval(int quizId);

    boolean updateQuizStatus(int quizId, QuizStatus status);

    int getQuizCountByCreator(int creatorId);

    int getQuizCountByCreatorAndStatus(int creatorId, QuizStatus status);

    int getQuestionCountByQuizId(int quizId);

    int getTotalQuestionsCountByCreator(int creatorId);

    int getTotalAttemptsCountByCreator(int creatorId);
}

