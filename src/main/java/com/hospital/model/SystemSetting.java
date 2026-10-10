package com.hospital.model;

import java.time.LocalDateTime;

public class SystemSetting {
    private String settingKey;
    private String settingValue;
    private String description;
    private LocalDateTime updatedAt;

    public SystemSetting() {}

    public SystemSetting(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    public SystemSetting(String settingKey, String settingValue, String description, LocalDateTime updatedAt) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
