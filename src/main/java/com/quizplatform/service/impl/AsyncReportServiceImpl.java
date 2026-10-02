package com.quizplatform.service.impl;

import com.quizplatform.model.report.CreatorPerformanceSummary;
import com.quizplatform.model.report.ParticipantPerformanceSummary;
import com.quizplatform.model.report.PlatformPerformanceSummary;
import com.quizplatform.model.report.QuestionAnalytics;
import com.quizplatform.service.AsyncReportService;
import com.quizplatform.service.PerformanceReportService;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of AsyncReportService demonstrating:
 * - Multithreading via Java Standard Library ExecutorService
 * - Callable<T> task submission returning Future<T>
 * - Background Runnable execution
 * - Thread-safe Queue<T> (ConcurrentLinkedQueue) for task tracking
 * - Controlled thread pool lifecycle and clean shutdown
 */
public class AsyncReportServiceImpl implements AsyncReportService {

    private final PerformanceReportService reportService;
    private final ExecutorService executorService;
    private final Queue<String> taskExecutionQueue = new ConcurrentLinkedQueue<>();

    public AsyncReportServiceImpl() {
        this(new PerformanceReportServiceImpl(), createManagedThreadPool(3));
    }

    public AsyncReportServiceImpl(PerformanceReportService reportService) {
        this(reportService, createManagedThreadPool(3));
    }

    public AsyncReportServiceImpl(PerformanceReportService reportService, ExecutorService executorService) {
        this.reportService = reportService;
        this.executorService = executorService;
    }

    private static ExecutorService createManagedThreadPool(int poolSize) {
        AtomicInteger threadNumber = new AtomicInteger(1);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "QuizReportWorker-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newFixedThreadPool(poolSize, threadFactory);
    }

    @Override
    public Future<ParticipantPerformanceSummary> getParticipantSummaryAsync(int participantId) {
        // Demonstrates Callable<T> and Future<T>
        Callable<ParticipantPerformanceSummary> task = () -> {
            taskExecutionQueue.add("ParticipantSummary-User-" + participantId);
            return reportService.getParticipantSummary(participantId);
        };
        return executorService.submit(task);
    }

    @Override
    public Future<PlatformPerformanceSummary> getPlatformSummaryAsync() {
        // Demonstrates Callable<T>
        Callable<PlatformPerformanceSummary> task = () -> {
            taskExecutionQueue.add("PlatformSummary");
            return reportService.getPlatformSummary();
        };
        return executorService.submit(task);
    }

    @Override
    public Future<CreatorPerformanceSummary> getCreatorSummaryAsync(int creatorId) {
        Callable<CreatorPerformanceSummary> task = () -> {
            taskExecutionQueue.add("CreatorSummary-User-" + creatorId);
            return reportService.getCreatorSummary(creatorId);
        };
        return executorService.submit(task);
    }

    @Override
    public Future<List<QuestionAnalytics>> getQuizQuestionAnalyticsAsync(int quizId, int creatorId) {
        Callable<List<QuestionAnalytics>> task = () -> {
            taskExecutionQueue.add("QuizAnalytics-Quiz-" + quizId);
            return reportService.getQuizQuestionAnalytics(quizId, creatorId);
        };
        return executorService.submit(task);
    }

    @Override
    public void executeBackgroundTask(Runnable task) {
        if (task != null) {
            executorService.submit(() -> {
                taskExecutionQueue.add("BackgroundTask-" + System.currentTimeMillis());
                task.run();
            });
        }
    }

    @Override
    public Queue<String> getTaskExecutionQueue() {
        return taskExecutionQueue;
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }
}
