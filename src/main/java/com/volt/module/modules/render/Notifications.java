package com.volt.module.modules.render;

import com.volt.Volt;
import com.volt.event.impl.render.EventRender2D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.utils.font.FontManager;
import com.volt.utils.font.fonts.FontRenderer;
import com.volt.utils.notification.Notification;
import com.volt.utils.notification.NotificationManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class Notifications extends Module {

    private static final int NOTIFICATION_WIDTH = 150;
    private static final int NOTIFICATION_HEIGHT = 40;
    private static final int NOTIFICATION_SPACING = 3;
    private static final int MARGIN_RIGHT = 8;
    private static final int MARGIN_BOTTOM = 8;
    
    private static final Color PURPLE_COLOR = new Color(128, 0, 128);
    private static final Color BLACK_COLOR = new Color(20, 20, 20, 255);
    private static final Color ENABLED_COLOR = new Color(0, 255, 0);
    private static final Color DISABLED_COLOR = new Color(255, 0, 0);

    public Notifications() {
        super("Notifications", "Toggle notification display on/off", Category.RENDER);
    }

    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        
        List<Notification> notifications = NotificationManager.getInstance().getNotifications();
        if (notifications.isEmpty()) {
            return;
        }
        
        updateNotifications(notifications);
        renderNotifications(event.getContext(), event.getWidth(), event.getHeight(), notifications);
    }
    
    private void updateNotifications(List<Notification> notifications) {
        Iterator<Notification> iterator = notifications.iterator();
        while (iterator.hasNext()) {
            Notification notification = iterator.next();
            
            if (notification.isExpired() && !notification.isRemoving()) {
                notification.setRemoving(true);
            }
            
            float targetProgress = notification.isRemoving() ? 0f : 1f;
            notification.setAnimationProgress(MathHelper.lerp(0.15f, notification.getAnimationProgress(), targetProgress));
            
            if (notification.isRemoving() && notification.getAnimationProgress() < 0.05f) {
                iterator.remove();
            }
        }
    }
    
    private void renderNotifications(DrawContext context, int screenWidth, int screenHeight, List<Notification> notifications) {
        int yOffset = 0;
        
        for (int i = notifications.size() - 1; i >= 0; i--) {
            Notification notification = notifications.get(i);
            
            int x = screenWidth - NOTIFICATION_WIDTH - MARGIN_RIGHT;
            int y = screenHeight - MARGIN_BOTTOM - NOTIFICATION_HEIGHT - yOffset;
            
            float slideProgress = notification.getAnimationProgress();
            int slideOffset = (int) ((1f - slideProgress) * (NOTIFICATION_WIDTH + 20));
            x += slideOffset;
            
            renderNotification(context, notification, x, y);
            
            yOffset += (int) ((NOTIFICATION_HEIGHT + NOTIFICATION_SPACING) * slideProgress);
        }
    }
    
    private void renderNotification(DrawContext context, Notification notification, int x, int y) {
        int alpha = (int) (notification.getAnimationProgress() * 255);
        
        context.fill(x, y, x + NOTIFICATION_WIDTH, y + NOTIFICATION_HEIGHT, BLACK_COLOR.getRGB());
        
        Color accentColor = new Color(PURPLE_COLOR.getRed(), PURPLE_COLOR.getGreen(), 
            PURPLE_COLOR.getBlue(), alpha);
        context.fill(x, y, x + 3, y + NOTIFICATION_HEIGHT, accentColor.getRGB());
        
        Color statusColor = notification.getType() == Notification.NotificationType.MODULE_ENABLED ? 
            new Color(ENABLED_COLOR.getRed(), ENABLED_COLOR.getGreen(), ENABLED_COLOR.getBlue(), alpha) :
            new Color(DISABLED_COLOR.getRed(), DISABLED_COLOR.getGreen(), DISABLED_COLOR.getBlue(), alpha);
        
        FontRenderer interFont = Volt.INSTANCE.fontManager.getSize(12, FontManager.Type.Inter);
        
        interFont.drawString(context.getMatrices(), notification.getTitle(), x + 8f, y + 8f, 
            new Color(255, 255, 255, alpha));
        interFont.drawString(context.getMatrices(), notification.getMessage(), x + 8f, y + 24f, 
            statusColor);
        
        float progressWidth = NOTIFICATION_WIDTH * (1f - notification.getLifetimeProgress());
        if (progressWidth > 0) {
            Color progressColor = new Color(accentColor.getRed(), accentColor.getGreen(), 
                accentColor.getBlue(), alpha / 3);
            context.fill(x, y + NOTIFICATION_HEIGHT - 2, 
                x + (int) progressWidth, y + NOTIFICATION_HEIGHT, progressColor.getRGB());
        }
    }
}