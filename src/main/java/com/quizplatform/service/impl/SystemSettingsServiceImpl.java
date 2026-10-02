package com.quizplatform.service.impl;

import com.quizplatform.dao.SystemSettingDAO;
import com.quizplatform.dao.impl.SystemSettingDAOImpl;
import com.quizplatform.exception.ValidationException;
import com.quizplatform.model.SystemSetting;
import com.quizplatform.model.UserRole;
import com.quizplatform.service.SystemSettingsService;
import com.quizplatform.util.SessionManager;
import com.quizplatform.util.ValidationUtil;
import java.util.List;
import java.util.Map;

/**
 * Implementation of SystemSettingsService.
 * Enforces role-based authorization: only ADMIN can modify system settings.
 */
public class SystemSettingsServiceImpl implements SystemSettingsService {

    public static final String KEY_LEADERBOARD = "leaderboard_enabled";
    public static final String KEY_REMINDERS = "reminders_enabled";
    public static final String KEY_QUIZ_ATTEMPTS = "quiz_attempts_enabled";
    public static final String KEY_MESSAGING = "messaging_enabled";
    public static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private final SystemSettingDAO settingDAO;

    public SystemSettingsServiceImpl() {
        this(new SystemSettingDAOImpl());
    }

    public SystemSettingsServiceImpl(SystemSettingDAO settingDAO) {
        this.settingDAO = settingDAO;
    }

    private void checkAdminAuthorization() {
        if (SessionManager.isLoggedIn() && !SessionManager.hasRole(UserRole.ADMIN)) {
            throw new SecurityException("Access Denied: Only administrators can modify system settings.");
        }
    }

    @Override
    public List<SystemSetting> getAllSettings() {
        // Ensure standard settings are seeded
        ensureDefaultSettings();
        return settingDAO.findAll();
    }

    @Override
    public SystemSetting getSettingByKey(String key) {
        if (ValidationUtil.isNullOrBlank(key)) {
            return null;
        }
        return settingDAO.findByKey(key.trim());
    }

    @Override
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        SystemSetting setting = getSettingByKey(key);
        if (setting == null || setting.getSettingValue() == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(setting.getSettingValue().trim());
    }

    @Override
    public int getIntSetting(String key, int defaultValue) {
        SystemSetting setting = getSettingByKey(key);
        if (setting == null || setting.getSettingValue() == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(setting.getSettingValue().trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean updateSetting(String key, String value, String description) {
        checkAdminAuthorization();

        if (ValidationUtil.isNullOrBlank(key)) {
            throw new ValidationException("Setting key cannot be null or empty.");
        }
        if (value == null) {
            throw new ValidationException("Setting value cannot be null.");
        }

        String trimmedKey = key.trim();
        String trimmedVal = value.trim();

        SystemSetting setting = settingDAO.findByKey(trimmedKey);
        if (setting == null) {
            setting = new SystemSetting(trimmedKey, trimmedVal, description != null ? description.trim() : "");
        } else {
            setting.setSettingValue(trimmedVal);
            if (description != null && !description.trim().isEmpty()) {
                setting.setDescription(description.trim());
            }
        }

        return settingDAO.saveOrUpdate(setting);
    }

    @Override
    public boolean updateSettings(Map<String, String> settings) {
        checkAdminAuthorization();
        if (settings == null || settings.isEmpty()) {
            return false;
        }
        boolean allUpdated = true;
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            boolean success = updateSetting(entry.getKey(), entry.getValue(), null);
            if (!success) {
                allUpdated = false;
            }
        }
        return allUpdated;
    }

    @Override
    public boolean isLeaderboardEnabled() {
        return getBooleanSetting(KEY_LEADERBOARD, true);
    }

    @Override
    public void setLeaderboardEnabled(boolean enabled) {
        updateSetting(KEY_LEADERBOARD, String.valueOf(enabled), "Global flag to enable or disable public leaderboard");
    }

    @Override
    public boolean isRemindersEnabled() {
        return getBooleanSetting(KEY_REMINDERS, true);
    }

    @Override
    public void setRemindersEnabled(boolean enabled) {
        updateSetting(KEY_REMINDERS, String.valueOf(enabled), "Global flag to enable or disable quiz reminders");
    }

    @Override
    public boolean isQuizAttemptsEnabled() {
        return getBooleanSetting(KEY_QUIZ_ATTEMPTS, true);
    }

    @Override
    public void setQuizAttemptsEnabled(boolean enabled) {
        updateSetting(KEY_QUIZ_ATTEMPTS, String.valueOf(enabled), "Global flag to enable or disable new quiz attempts");
    }

    @Override
    public boolean isMessagingEnabled() {
        return getBooleanSetting(KEY_MESSAGING, true);
    }

    @Override
    public void setMessagingEnabled(boolean enabled) {
        updateSetting(KEY_MESSAGING, String.valueOf(enabled), "Global flag to enable or disable platform messaging");
    }

    @Override
    public boolean isNotificationsEnabled() {
        return getBooleanSetting(KEY_NOTIFICATIONS, true);
    }

    @Override
    public void setNotificationsEnabled(boolean enabled) {
        updateSetting(KEY_NOTIFICATIONS, String.valueOf(enabled), "Global flag to enable or disable platform notifications");
    }

    private void ensureDefaultSettings() {
        checkAndSeed(KEY_LEADERBOARD, "true", "Global flag to enable or disable public leaderboard");
        checkAndSeed(KEY_REMINDERS, "true", "Global flag to enable or disable quiz reminders");
        checkAndSeed(KEY_QUIZ_ATTEMPTS, "true", "Global flag to enable or disable new quiz attempts");
        checkAndSeed(KEY_MESSAGING, "true", "Global flag to enable or disable platform messaging");
        checkAndSeed(KEY_NOTIFICATIONS, "true", "Global flag to enable or disable platform notifications");
    }

    private void checkAndSeed(String key, String defaultValue, String description) {
        if (!settingDAO.existsByKey(key)) {
            SystemSetting s = new SystemSetting(key, defaultValue, description);
            settingDAO.createSetting(s);
        }
    }
}
