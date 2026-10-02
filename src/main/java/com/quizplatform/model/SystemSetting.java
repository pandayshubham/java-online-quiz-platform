package com.quizplatform.model;

import java.time.LocalDateTime;

/**
 * Model representing a configurable system setting.
 */
public class SystemSetting {

    private int id;
    private String settingKey;
    private String settingValue;
    private String description;
    private LocalDateTime updatedAt;

    public SystemSetting() {
    }

    public SystemSetting(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    public SystemSetting(int id, String settingKey, String settingValue, String description, LocalDateTime updatedAt) {
        this.id = id;
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "SystemSetting{" +
                "id=" + id +
                ", settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                ", description='" + description + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
