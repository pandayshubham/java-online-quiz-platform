package com.quizplatform.service.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Concurrency coordinator that synchronizes quiz attempt submissions.
 * 
 * Demonstrates:
 * - Multithreading & Synchronization (ReentrantLock)
 * - Atomic concurrent operations (ConcurrentHashMap)
 * - Race condition prevention during critical attempt finalization
 * - Complementary protection alongside JDBC transactions
 */
public class AttemptSubmissionCoordinator {

    // Lock registry per attemptId to serialize submissions targeting the same attempt
    private final ConcurrentMap<Integer, ReentrantLock> attemptLocks = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface SubmissionAction<T> {
        T execute() throws Exception;
    }

    /**
     * Executes the critical quiz-attempt submission action under a dedicated lock for the attempt.
     * Prevents two concurrent threads from finalizing the same attempt simultaneously.
     *
     * @param attemptId Unique ID of the quiz attempt
     * @param action    The submission logic (validation + database transaction)
     * @param <T>       Return type
     * @return Result of the submission action
     */
    public <T> T coordinateSubmission(int attemptId, SubmissionAction<T> action) {
        // Demonstrates synchronization: ReentrantLock per attempt
        ReentrantLock lock = attemptLocks.computeIfAbsent(attemptId, k -> new ReentrantLock());
        lock.lock();
        try {
            return action.execute();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during synchronized attempt submission: " + e.getMessage(), e);
        } finally {
            lock.unlock();
            // Clean up completed locks when no threads are queued
            if (!lock.hasQueuedThreads()) {
                attemptLocks.remove(attemptId, lock);
            }
        }
    }

    /**
     * Checks if a specific attempt is currently being processed under lock.
     */
    public boolean isAttemptLocked(int attemptId) {
        ReentrantLock lock = attemptLocks.get(attemptId);
        return lock != null && lock.isLocked();
    }
}
