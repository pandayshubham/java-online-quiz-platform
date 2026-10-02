package com.quizplatform.service;

import com.quizplatform.model.report.CreatorPerformanceSummary;
import com.quizplatform.model.report.ParticipantPerformanceSummary;
import com.quizplatform.model.report.PlatformPerformanceSummary;
import com.quizplatform.model.report.QuestionAnalytics;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

/**
 * Service interface for asynchronous report and analytics processing.
 * Demonstrates Java Multithreading with ExecutorService, Callable, and Future.
 */
public interface AsyncReportService {

    /**
     * Asynchronously generates participant performance summary without blocking.
     * Demonstrates Callable<T> and Future<T>.
     */
    Future<ParticipantPerformanceSummary> getParticipantSummaryAsync(int participantId);

    /**
     * Asynchronously generates platform-wide performance summary.
     */
    Future<PlatformPerformanceSummary> getPlatformSummaryAsync();

    /**
     * Asynchronously generates creator performance summary.
     */
    Future<CreatorPerformanceSummary> getCreatorSummaryAsync(int creatorId);

    /**
     * Asynchronously calculates question analytics for a quiz.
     */
    Future<List<QuestionAnalytics>> getQuizQuestionAnalyticsAsync(int quizId, int creatorId);

    /**
     * Submits a background Runnable task to the managed thread pool.
     */
    void executeBackgroundTask(Runnable task);

    /**
     * Returns the thread-safe queue of executed task identifiers.
     * Demonstrates Queue<T> integration with multithreading.
     */
    Queue<String> getTaskExecutionQueue();

    /**
     * Cleanly shuts down the underlying ExecutorService.
     */
    void shutdown();

    /**
     * Checks if the thread pool has been shut down.
     */
    boolean isShutdown();
}
