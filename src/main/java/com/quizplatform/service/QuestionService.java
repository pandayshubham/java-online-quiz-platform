package com.quizplatform.service;

import com.quizplatform.model.Option;
import com.quizplatform.model.Question;
import java.util.List;

/**
 * Service interface for question and option management.
 */
public interface QuestionService {

    Question getQuestionById(int id);

    List<Question> getQuestionsByQuizId(int quizId);

    List<Option> getOptionsByQuestionId(int questionId);

    Option getCorrectOptionByQuestionId(int questionId);

    Question createQuestion(Question question, List<Option> options);

    boolean updateQuestion(Question question, List<Option> options);

    boolean deleteQuestion(int questionId);

    int getQuestionCountByQuizId(int quizId);

    int getNextQuestionOrder(int quizId);
}
