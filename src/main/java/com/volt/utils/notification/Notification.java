package com.volt.utils.notification;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Notification {
    private String title;
    private String message;
    private NotificationType type;
    private long creationTime;
    private long duration;
    private float animationProgress;
    private boolean isRemoving;

    public Notification(String title, String message, NotificationType type, long duration) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.duration = duration;
        this.creationTime = System.currentTimeMillis();
        this.animationProgress = 0f;
        this.isRemoving = false;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - creationTime > duration;
    }

    public float getLifetimeProgress() {
        long elapsed = System.currentTimeMillis() - creationTime;
        return Math.min(1f, (float) elapsed / duration);
    }

    public enum NotificationType {
        MODULE_ENABLED,
        MODULE_DISABLED
    }
}