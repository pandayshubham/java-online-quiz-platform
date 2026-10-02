package com.quizplatform.service.impl;

import com.quizplatform.dao.LeaderboardDAO;
import com.quizplatform.dao.SystemSettingDAO;
import com.quizplatform.dao.impl.LeaderboardDAOImpl;
import com.quizplatform.dao.impl.SystemSettingDAOImpl;
import com.quizplatform.model.LeaderboardEntry;
import com.quizplatform.model.SystemSetting;
import com.quizplatform.service.LeaderboardService;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of LeaderboardService.
 */
public class LeaderboardServiceImpl implements LeaderboardService {

    private static final String SETTING_KEY_LEADERBOARD = "leaderboard_enabled";

    private final LeaderboardDAO leaderboardDAO;
    private final SystemSettingDAO systemSettingDAO;

    public LeaderboardServiceImpl() {
        this(new LeaderboardDAOImpl(), new SystemSettingDAOImpl());
    }

    public LeaderboardServiceImpl(LeaderboardDAO leaderboardDAO, SystemSettingDAO systemSettingDAO) {
        this.leaderboardDAO = leaderboardDAO;
        this.systemSettingDAO = systemSettingDAO;
    }

    @Override
    public boolean isLeaderboardEnabled() {
        SystemSetting setting = systemSettingDAO.findByKey(SETTING_KEY_LEADERBOARD);
        if (setting == null || setting.getSettingValue() == null) {
            return true; // Default to true if not configured
        }
        return Boolean.parseBoolean(setting.getSettingValue().trim());
    }

    @Override
    public void setLeaderboardEnabled(boolean enabled) {
        if (com.quizplatform.util.SessionManager.isLoggedIn() && !com.quizplatform.util.SessionManager.hasRole(com.quizplatform.model.UserRole.ADMIN)) {
            throw new SecurityException("Access Denied: Only administrators can modify system settings.");
        }
        SystemSetting setting = systemSettingDAO.findByKey(SETTING_KEY_LEADERBOARD);
        if (setting == null) {
            setting = new SystemSetting(SETTING_KEY_LEADERBOARD, String.valueOf(enabled), "Global flag to enable or disable public leaderboard");
        } else {
            setting.setSettingValue(String.valueOf(enabled));
        }
        systemSettingDAO.saveOrUpdate(setting);
    }

    @Override
    public List<LeaderboardEntry> getQuizLeaderboard(int quizId) {
        if (!isLeaderboardEnabled()) {
            return Collections.emptyList();
        }
        return leaderboardDAO.getQuizLeaderboard(quizId);
    }

    @Override
    public List<LeaderboardEntry> getGlobalLeaderboard() {
        if (!isLeaderboardEnabled()) {
            return Collections.emptyList();
        }
        return leaderboardDAO.getGlobalLeaderboard();
    }
}
