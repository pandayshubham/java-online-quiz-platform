package com.quizplatform.service;

import com.quizplatform.model.LeaderboardEntry;
import java.util.List;

/**
 * Service interface for managing and displaying participant leaderboards.
 */
public interface LeaderboardService {

    /**
     * Checks if the leaderboard feature is currently enabled platform-wide.
     *
     * @return true if enabled, false otherwise
     */
    boolean isLeaderboardEnabled();

    /**
     * Enables or disables the leaderboard feature via system settings.
     *
     * @param enabled true to enable, false to disable
     */
    void setLeaderboardEnabled(boolean enabled);

    /**
     * Retrieves the leaderboard for a specific quiz.
     * If the leaderboard is disabled, returns an empty list.
     *
     * @param quizId ID of the quiz
     * @return ranked list of leaderboard entries
     */
    List<LeaderboardEntry> getQuizLeaderboard(int quizId);

    /**
     * Retrieves the platform-wide global leaderboard.
     * If the leaderboard is disabled, returns an empty list.
     *
     * @return ranked list of global leaderboard entries
     */
    List<LeaderboardEntry> getGlobalLeaderboard();
}
