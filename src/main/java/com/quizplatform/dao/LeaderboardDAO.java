package com.quizplatform.dao;

import com.quizplatform.model.LeaderboardEntry;
import java.util.List;

/**
 * Data Access Object interface for platform and quiz-specific leaderboards.
 */
public interface LeaderboardDAO {

    /**
     * Retrieves the leaderboard for a specific quiz, ranking participants by best score / percentage.
     *
     * @param quizId the ID of the quiz
     * @return list of ranked leaderboard entries
     */
    List<LeaderboardEntry> getQuizLeaderboard(int quizId);

    /**
     * Retrieves the platform-wide global leaderboard, ranking participants across all completed quizzes.
     *
     * @return list of global leaderboard entries
     */
    List<LeaderboardEntry> getGlobalLeaderboard();
}
