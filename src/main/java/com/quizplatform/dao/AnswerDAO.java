package com.quizplatform.dao;

import com.quizplatform.model.Answer;
import java.util.List;

/**
 * Data Access Object interface for Answer entities.
 */
public interface AnswerDAO {

    int createAnswer(Answer answer);

    int createAnswer(java.sql.Connection conn, Answer answer);

    Answer findById(int id);

    List<Answer> findByAttemptId(int attemptId);

    boolean updateAnswer(Answer answer);

    boolean deleteAnswer(int id);

    // Reporting and statistics queries
    int getTotalAnswersCount();

    int getTotalAnswersCountByParticipant(int participantId);

    int getTotalCorrectAnswersByParticipant(int participantId);

    int getTotalIncorrectAnswersByParticipant(int participantId);

    int getTotalUnansweredByParticipant(int participantId);

    // Question-level analytics
    int getTimesAnsweredCountByQuestionId(int questionId);

    int getCorrectCountByQuestionId(int questionId);

    int getIncorrectCountByQuestionId(int questionId);

    int getUnansweredCountByQuestionId(int questionId);
}
