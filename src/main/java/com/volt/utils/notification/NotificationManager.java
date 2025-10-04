package com.volt.utils.notification;

import com.volt.module.Module;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private static final NotificationManager INSTANCE = new NotificationManager();
    @Getter
    private final List<Notification> notifications = new ArrayList<>();
    
    private static final long DEFAULT_DURATION = 3000;
    
    public static NotificationManager getInstance() {
        return INSTANCE;
    }

    public void addModuleNotification(Module module, boolean enabled) {
        String title = module.getName();
        String message = enabled ? "Enabled" : "Disabled";
        Notification.NotificationType type = enabled ? 
            Notification.NotificationType.MODULE_ENABLED : 
            Notification.NotificationType.MODULE_DISABLED;
        
        Notification notification = new Notification(title, message, type, DEFAULT_DURATION);
        notifications.add(notification);
    }
}