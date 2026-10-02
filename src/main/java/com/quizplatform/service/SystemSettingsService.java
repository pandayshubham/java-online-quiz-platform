package com.quizplatform.service;

import com.quizplatform.model.SystemSetting;
import java.util.List;
import java.util.Map;

/**
 * Service interface for global platform settings and feature toggles.
 */
public interface SystemSettingsService {

    List<SystemSetting> getAllSettings();

    SystemSetting getSettingByKey(String key);

    boolean getBooleanSetting(String key, boolean defaultValue);

    int getIntSetting(String key, int defaultValue);

    boolean updateSetting(String key, String value, String description);

    boolean updateSettings(Map<String, String> settings);

    boolean isLeaderboardEnabled();

    void setLeaderboardEnabled(boolean enabled);

    boolean isRemindersEnabled();

    void setRemindersEnabled(boolean enabled);

    boolean isQuizAttemptsEnabled();

    void setQuizAttemptsEnabled(boolean enabled);

    boolean isMessagingEnabled();

    void setMessagingEnabled(boolean enabled);

    boolean isNotificationsEnabled();

    void setNotificationsEnabled(boolean enabled);
}
