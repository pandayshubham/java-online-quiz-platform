package com.quizplatform.dao;

import com.quizplatform.model.Question;
import java.util.List;

/**
 * Data Access Object interface for Question entities.
 */
public interface QuestionDAO {

    int createQuestion(Question question);

    Question findById(int id);

    List<Question> findByQuizId(int quizId);

    List<Question> findByQuizId(java.sql.Connection conn, int quizId);

    boolean updateQuestion(Question question);

    boolean deleteQuestion(int id);

    int getQuestionCountByQuizId(int quizId);

    int getMaxQuestionOrderByQuizId(int quizId);

    int getTotalQuestionsCount();
}

