package com.quizplatform.dao;

import com.quizplatform.model.QuizAttempt;
import java.util.List;

/**
 * Data Access Object interface for QuizAttempt entities and statistics.
 */
public interface QuizAttemptDAO {

    int createAttempt(QuizAttempt attempt);

    QuizAttempt findById(int id);

    List<QuizAttempt> findByParticipantId(int participantId);

    List<QuizAttempt> findByQuizId(int quizId);

    List<QuizAttempt> findAll();

    boolean updateAttempt(QuizAttempt attempt);

    // Transaction-aware methods
    QuizAttempt findById(java.sql.Connection conn, int id);

    boolean updateAttempt(java.sql.Connection conn, QuizAttempt attempt);

    QuizAttempt findActiveAttemptByParticipantAndQuiz(int participantId, int quizId);

    // Statistical queries for analytics and reports
    double getAverageScoreByQuizId(int quizId);

    int getHighestScoreByQuizId(int quizId);

    int getLowestScoreByQuizId(int quizId);

    int getTotalAttemptsCount();

    int getCompletedAttemptsCount();

    int getTimeExpiredAttemptsCount();

    double getPlatformAverageScore();

    List<QuizAttempt> findRecentAttempts(int limit);

    List<QuizAttempt> findByCreatorId(int creatorId);

    double getAverageScoreByCreatorId(int creatorId);

    int getHighestScoreByCreatorId(int creatorId);

    int getLowestScoreByCreatorId(int creatorId);

    double getAveragePercentageByCreatorId(int creatorId);

    double getHighestPercentageByCreatorId(int creatorId);

    double getLowestPercentageByCreatorId(int creatorId);

    int getTimeExpiredAttemptsCountByCreator(int creatorId);

    int getAttemptsCountByParticipant(int participantId);

    int getCompletedAttemptsCountByParticipant(int participantId);

    int getTimeExpiredAttemptsCountByParticipant(int participantId);

    double getAveragePercentageByParticipant(int participantId);

    int getHighestScoreByParticipant(int participantId);

    List<QuizAttempt> findByParticipantIdAndQuizId(int participantId, int quizId);
}


