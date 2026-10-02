package com.quizplatform.service;

import com.quizplatform.model.QuizAttempt;
import java.util.List;

/**
 * Service interface for quiz attempts and tracking.
 */
public interface QuizAttemptService {

    QuizAttempt getAttemptById(int id);

    List<QuizAttempt> getAttemptsByParticipant(int participantId);

    List<QuizAttempt> getAttemptsByQuiz(int quizId);

    int startAttempt(int quizId, int participantId);

    QuizAttempt submitAttempt(int attemptId, java.util.Map<Integer, Integer> questionToOptionMap, boolean timeExpired);

    List<com.quizplatform.model.Answer> getAnswersByAttempt(int attemptId);

    int getAttemptsCountByParticipant(int participantId);

    int getCompletedAttemptsCountByParticipant(int participantId);

    double getAveragePercentageByParticipant(int participantId);

    int getHighestScoreByParticipant(int participantId);

    List<QuizAttempt> getAttemptsByCreator(int creatorId);

    double getAverageScoreByCreator(int creatorId);

    int getHighestScoreByCreator(int creatorId);

    int getLowestScoreByCreator(int creatorId);

    double getAverageScoreByQuiz(int quizId);

    int getHighestScoreByQuiz(int quizId);

    int getLowestScoreByQuiz(int quizId);

    QuizAttempt getActiveAttempt(int quizId, int participantId);

    List<QuizAttempt> getAttemptsByParticipantAndQuiz(int participantId, int quizId);

    int getTimeExpiredAttemptsCount();

    double getPlatformAverageScore();

    List<QuizAttempt> getRecentAttempts(int limit);
}


