package com.quizplatform.service;

import com.quizplatform.model.QuizAttempt;
import com.quizplatform.model.report.AttemptPerformanceDetail;
import com.quizplatform.model.report.CreatorPerformanceSummary;
import com.quizplatform.model.report.ParticipantPerformanceSummary;
import com.quizplatform.model.report.PlatformPerformanceSummary;
import com.quizplatform.model.report.QuestionAnalytics;
import com.quizplatform.model.report.QuizPerformanceSummary;
import java.util.List;

/**
 * Service interface for generating comprehensive performance reports
 * for participants, quiz creators, and system administrators.
 */
public interface PerformanceReportService {

    ParticipantPerformanceSummary getParticipantSummary(int participantId);

    List<QuizAttempt> getParticipantHistory(int participantId);

    AttemptPerformanceDetail getAttemptPerformance(int attemptId, int participantId);

    QuizPerformanceSummary getParticipantQuizPerformance(int participantId, int quizId);

    CreatorPerformanceSummary getCreatorSummary(int creatorId);

    QuizPerformanceSummary getCreatorQuizPerformance(int creatorId, int quizId);

    List<QuizAttempt> getCreatorAttempts(int creatorId);

    List<QuestionAnalytics> getQuizQuestionAnalytics(int quizId, int creatorId);

    PlatformPerformanceSummary getPlatformSummary();

    List<QuizAttempt> getRecentPlatformAttempts(int limit);
}
