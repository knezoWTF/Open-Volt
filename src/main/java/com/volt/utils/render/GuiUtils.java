package com.volt.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class GuiUtils {

    private static final Map<String, AnimationState> animations = new HashMap<>();

    public static float animateFloat(String id, float target, float speed, EasingType easing) {
        AnimationState state = animations.computeIfAbsent(id,
                k -> new AnimationState(target, speed, easing));

        state.targetValue = target;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - state.lastUpdate) / 1000.0f;
        state.lastUpdate = currentTime;

        if (Math.abs(state.currentValue - state.targetValue) > 0.01f) {
            float difference = state.targetValue - state.currentValue;
            float step = difference * state.speed * deltaTime;

            step = applyEasing(step, state.easing);
            state.currentValue += step;

            if (Math.abs(state.currentValue - state.targetValue) < 0.01f) {
                state.currentValue = state.targetValue;
            }
        }

        return state.currentValue;
    }

    private static float applyEasing(float t, EasingType easing) {
        return switch (easing) {
            case LINEAR -> t;
            case EASE_IN -> t * t;
            case EASE_OUT -> 1 - (1 - t) * (1 - t);
            case EASE_IN_OUT -> t < 0.5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
            case BOUNCE -> bounceEasing(t);
            case ELASTIC -> elasticEasing(t);
        };
    }

    private static float bounceEasing(float t) {
        if (t < 1 / 2.75f) {
            return 7.5625f * t * t;
        } else if (t < 2 / 2.75f) {
            return 7.5625f * (t -= 1.5f / 2.75f) * t + 0.75f;
        } else if (t < 2.5 / 2.75f) {
            return 7.5625f * (t -= 2.25f / 2.75f) * t + 0.9375f;
        } else {
            return 7.5625f * (t -= 2.625f / 2.75f) * t + 0.984375f;
        }
    }

    private static float elasticEasing(float t) {
        return (float) (Math.pow(2, -10 * t) * Math.sin((t - 0.1) * (2 * Math.PI) / 0.4) + 1);
    }

    public static void drawGradientRect(DrawContext context, int x, int y, int width, int height,
                                        Color topLeft, Color bottomLeft) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        context.fillGradient(x, y, x + width, y + height, topLeft.getRGB(), bottomLeft.getRGB());

        RenderSystem.disableBlend();
    }

    public static void drawProgressBar(DrawContext context, int x, int y, int width, int height,
                                       float progress, Color backgroundColor, Color progressColor) {
        progress = Math.max(0, Math.min(1, progress));

        RenderUtils.drawSmoothRoundedRect(context, x, y, width, height, height / 2, backgroundColor.getRGB());

        if (progress > 0) {
            int progressWidth = (int) (width * progress);
            RenderUtils.drawSmoothRoundedRect(context, x, y, progressWidth, height, height / 2, progressColor.getRGB());
        }

        Color highlight = new Color(255, 255, 255, 30);
        RenderUtils.drawSmoothRoundedRect(context, x, y, width, 1, height / 2, highlight.getRGB());
    }

    public static void drawButton(DrawContext context, int x, int y, int width, int height,
                                  String text, boolean hovered, boolean pressed) {
        Color bgColor = pressed ? Colors.BACKGROUND_LIGHT :
                hovered ? Colors.BACKGROUND_LIGHT : Colors.BACKGROUND_DARK;

        RenderUtils.drawSmoothRoundedRect(context, x, y, width, height, 6, bgColor.getRGB());

        Color borderColor = hovered ? Colors.ACCENT_BLUE : Colors.BORDER_LIGHT;
        drawBorder(context, x, y, width, height, 6, 1, borderColor);

        drawCenteredText(context, text, x + width / 2, y + height / 2, Colors.TEXT_PRIMARY);
    }

    public static void drawSlider(DrawContext context, int x, int y, int width, int height,
                                  float value, boolean dragging) {
        value = Math.max(0, Math.min(1, value));

        Color trackColor = new Color(40, 40, 40);
        RenderUtils.drawSmoothRoundedRect(context, x, y + height / 4, width, height / 2, height / 4, trackColor.getRGB());

        if (value > 0) {
            int fillWidth = (int) (width * value);
            RenderUtils.drawSmoothRoundedRect(context, x, y + height / 4, fillWidth, height / 2, height / 4, Colors.ACCENT_PURPLE.getRGB());
        }

        int handleX = x + (int) (width * value) - height / 2;
        handleX = Math.max(x, Math.min(x + width - height, handleX));
        Color handleColor = dragging ? Colors.ACCENT_PURPLE.brighter() : Colors.ACCENT_PURPLE;

        Color shadowColor = new Color(0, 0, 0, 60);
        RenderUtils.drawSmoothRoundedRect(context, handleX + 1, y + 1, height, height, height / 2, shadowColor.getRGB());
        RenderUtils.drawSmoothRoundedRect(context, handleX, y, height, height, height / 2, handleColor.getRGB());
    }

    public static void drawCheckbox(DrawContext context, int x, int y, int size, boolean checked, boolean hovered) {
        Color bgColor = checked ? Colors.ACCENT_PURPLE : Colors.BACKGROUND_DARK;
        Color borderColor = hovered ? Colors.ACCENT_PURPLE : Colors.BORDER_LIGHT;

        RenderUtils.drawSmoothRoundedRect(context, x, y, size, size, 3, bgColor.getRGB());

        drawBorder(context, x, y, size, size, 3, 1, borderColor);

        if (checked) {
            drawCheckmark(context, x + 2, y + 2, size - 4, Colors.TEXT_PRIMARY);
        }
    }

    public static void drawToggle(DrawContext context, int x, int y, int width, int height,
                                  boolean enabled, boolean hovered) {
        Color bgColor = enabled ? Colors.ACCENT_PURPLE : Colors.BACKGROUND_DARK;
        Color borderColor = hovered ? Colors.ACCENT_PURPLE.brighter() :
                enabled ? Colors.ACCENT_PURPLE : Colors.BORDER_LIGHT;

        RenderUtils.drawSmoothRoundedRect(context, x, y, width, height, height / 2, bgColor.getRGB());

        drawBorder(context, x, y, width, height, height / 2, 1, borderColor);

        int handleSize = height - 4;
        int handleX = enabled ? x + width - handleSize - 2 : x + 2;
        Color handleColor = Colors.TEXT_PRIMARY;
        RenderUtils.drawSmoothRoundedRect(context, handleX, y + 2, handleSize, handleSize, handleSize / 2, handleColor.getRGB());
    }

    public static void drawDropdown(DrawContext context, int x, int y, int width, int height,
                                    String selected, boolean expanded, boolean hovered) {
        Color bgColor = hovered ? Colors.BACKGROUND_LIGHT : Colors.BACKGROUND_DARK;

        RenderUtils.drawSmoothRoundedRect(context, x, y, width, height, 4, bgColor.getRGB());

        drawText(context, selected, x + 8, y + height / 2 - 4, Colors.TEXT_PRIMARY);

        drawArrow(context, x + width - 16, y + height / 2 - 3, 6, expanded, Colors.TEXT_SECONDARY);
    }

    public static void drawTooltip(DrawContext context, int x, int y, String text) {
        int padding = 6;
        int textWidth = getTextWidth(text);
        int textHeight = getTextHeight();

        int tooltipWidth = textWidth + padding * 2;
        int tooltipHeight = textHeight + padding * 2;

        Color shadowColor = new Color(0, 0, 0, 100);
        RenderUtils.drawSmoothRoundedRect(context, x + 2, y + 2, tooltipWidth, tooltipHeight, 4, shadowColor.getRGB());

        Color bgColor = new Color(25, 25, 25, 240);
        RenderUtils.drawSmoothRoundedRect(context, x, y, tooltipWidth, tooltipHeight, 4, bgColor.getRGB());


        drawText(context, text, x + padding, y + padding, Colors.TEXT_PRIMARY);
    }

    public static void drawNotification(DrawContext context, int x, int y, int width, String title,
                                        String message, NotificationType type, float progress) {
        Color accentColor = getNotificationColor(type);

        RenderUtils.drawSmoothRoundedRect(context, x, y, width, 60, 8, Colors.BACKGROUND_DARK.getRGB());

        RenderUtils.drawSmoothRoundedRect(context, x, y, 4, 60, 2, accentColor.getRGB());

        if (progress > 0) {
            int progressWidth = (int) (width * progress);
            Color progressColor = new Color(accentColor.getRed(), accentColor.getGreen(),
                    accentColor.getBlue(), 60);
            RenderUtils.drawSmoothRoundedRect(context, x, y + 56, progressWidth, 4, 2, progressColor.getRGB());
        }

        drawNotificationIcon(context, x + 12, y + 12, type);

        drawText(context, title, x + 40, y + 12, Colors.TEXT_PRIMARY);
        drawText(context, message, x + 40, y + 32, Colors.TEXT_SECONDARY);
    }

    private static Color getNotificationColor(NotificationType type) {
        switch (type) {
            case SUCCESS:
                return Colors.SUCCESS;
            case WARNING:
                return Colors.WARNING;
            case ERROR:
                return Colors.ERROR;
            default:
                return Colors.INFO;
        }
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height,
                                  int radius, int thickness, Color color) {
        RenderUtils.drawSmoothRoundedRect(context, x, y, width, thickness, radius, color.getRGB());
        RenderUtils.drawSmoothRoundedRect(context, x, y + height - thickness, width, thickness, radius, color.getRGB());
        RenderUtils.drawSmoothRoundedRect(context, x, y, thickness, height, radius, color.getRGB());
        RenderUtils.drawSmoothRoundedRect(context, x + width - thickness, y, thickness, height, radius, color.getRGB());
    }

    public static void drawText(DrawContext context, String text, int x, int y, Color color) {
        context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                text, x, y, color.getRGB(), false);
    }

    public static void drawCenteredText(DrawContext context, String text, int centerX, int centerY, Color color) {
        int textWidth = getTextWidth(text);
        int textHeight = getTextHeight();
        drawText(context, text, centerX - textWidth / 2, centerY - textHeight / 2, color);
    }

    public static void drawCheckmark(DrawContext context, int x, int y, int size, Color color) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;

        context.fill(centerX - 2, centerY, centerX, centerY + 2, color.getRGB());
        context.fill(centerX, centerY + 2, centerX + 4, centerY - 2, color.getRGB());
    }

    public static void drawArrow(DrawContext context, int x, int y, int size, boolean down, Color color) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;

        if (down) {
            for (int i = 0; i < size / 2; i++) {
                context.fill(centerX - i, centerY - size / 4 + i,
                        centerX + i + 1, centerY - size / 4 + i + 1, color.getRGB());
            }
        } else {
            for (int i = 0; i < size / 2; i++) {
                context.fill(centerX - i, centerY + size / 4 - i,
                        centerX + i + 1, centerY + size / 4 - i + 1, color.getRGB());
            }
        }
    }

    public static void drawNotificationIcon(DrawContext context, int x, int y, NotificationType type) {
        Color iconColor = getNotificationColor(type);

        switch (type) {
            case INFO:
                RenderUtils.drawFilledCircle(context, x + 8, y + 8, 8, iconColor.getRGB());
                drawText(context, "i", x + 6, y + 4, Colors.TEXT_PRIMARY);
                break;
            case SUCCESS:
                RenderUtils.drawFilledCircle(context, x + 8, y + 8, 8, iconColor.getRGB());
                drawCheckmark(context, x + 4, y + 4, 8, Colors.TEXT_PRIMARY);
                break;
            case WARNING:
                drawText(context, "!", x + 6, y + 2, iconColor);
                break;
            case ERROR:
                drawText(context, "X", x + 4, y + 4, iconColor);
                break;
        }
    }

    public static int getTextWidth(String text) {
        return net.minecraft.client.MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    public static int getTextHeight() {
        return net.minecraft.client.MinecraftClient.getInstance().textRenderer.fontHeight;
    }

    public static Color interpolateColor(Color color1, Color color2, float factor) {
        factor = Math.max(0, Math.min(1, factor));

        int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * factor);
        int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * factor);
        int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * factor);
        int a = (int) (color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * factor);

        return new Color(r, g, b, a);
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.max(0, Math.min(255, alpha)));
    }

    public static Color rainbow(float offset, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() * 0.001f + offset) % 1.0f;
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static void cleanupAnimations() {
        long currentTime = System.currentTimeMillis();
        animations.entrySet().removeIf(entry -> {
            AnimationState state = entry.getValue();
            return currentTime - state.lastUpdate > 5000 &&
                    Math.abs(state.currentValue - state.targetValue) < 0.01f;
        });
    }

    public enum EasingType {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, BOUNCE, ELASTIC
    }

    public enum NotificationType {
        INFO, SUCCESS, WARNING, ERROR
    }

    private static class AnimationState {
        float currentValue;
        float targetValue;
        float speed;
        long lastUpdate;
        EasingType easing;

        AnimationState(float initial, float speed, EasingType easing) {
            this.currentValue = initial;
            this.targetValue = initial;
            this.speed = speed;
            this.lastUpdate = System.currentTimeMillis();
            this.easing = easing;
        }
    }

    public static class Colors {
        public static final Color BACKGROUND_DARK = new Color(15, 15, 15, 180);
        public static final Color BACKGROUND_LIGHT = new Color(25, 25, 25, 200);
        public static final Color ACCENT_BLUE = new Color(150, 64, 255);
        public static final Color ACCENT_GREEN = new Color(76, 175, 80);
        public static final Color ACCENT_RED = new Color(244, 67, 54);
        public static final Color ACCENT_ORANGE = new Color(255, 152, 0);
        public static final Color ACCENT_PURPLE = new Color(156, 39, 176);
        public static final Color TEXT_PRIMARY = new Color(255, 255, 255);
        public static final Color TEXT_SECONDARY = new Color(180, 180, 180);
        public static final Color TEXT_DISABLED = new Color(120, 120, 120);
        public static final Color BORDER_LIGHT = new Color(60, 60, 60);
        public static final Color BORDER_DARK = new Color(30, 30, 30);
        public static final Color SUCCESS = new Color(76, 175, 80);
        public static final Color WARNING = new Color(255, 193, 7);
        public static final Color ERROR = new Color(244, 67, 54);
        public static final Color INFO = new Color(33, 150, 243);
    }

    public static class Layout {
        public static void drawGrid(DrawContext context, int x, int y, int width, int height,
                                    int cellWidth, int cellHeight, Color gridColor) {
            for (int i = 0; i <= width / cellWidth; i++) {
                int lineX = x + i * cellWidth;
                context.fill(lineX, y, lineX + 1, y + height, gridColor.getRGB());
            }

            for (int i = 0; i <= height / cellHeight; i++) {
                int lineY = y + i * cellHeight;
                context.fill(x, lineY, x + width, lineY + 1, gridColor.getRGB());
            }
        }

        public static void drawContainer(DrawContext context, int x, int y, int width, int height,
                                         String title, boolean collapsible, boolean collapsed) {
            Color bgColor = Colors.BACKGROUND_DARK;
            Color headerColor = Colors.BACKGROUND_LIGHT;

            int headerHeight = 24;
            RenderUtils.drawSmoothRoundedRect(context, x, y, width, headerHeight, 6, headerColor.getRGB());

            drawText(context, title, x + 8, y + 8, Colors.TEXT_PRIMARY);

            if (collapsible) {
                drawArrow(context, x + width - 20, y + 8, 8, collapsed, Colors.TEXT_SECONDARY);
            }

            if (!collapsed) {
                RenderUtils.drawSmoothRoundedRect(context, x, y + headerHeight, width,
                        height - headerHeight, 6, bgColor.getRGB());

                drawBorder(context, x, y, width, height, 6, 1, Colors.BORDER_LIGHT);

            }
        }
    }
}
