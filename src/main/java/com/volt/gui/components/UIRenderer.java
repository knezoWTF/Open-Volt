package com.volt.gui.components;

import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class UIRenderer {

    public static void renderGlow(DrawContext context, int x, int y, int width, int height, Color glowColor, float intensity) {
        for (int layer = 2; layer >= 0; layer--) {
            float alpha = intensity * (0.1f + 0.2f * (float) layer / 2);
            int expand = layer;

            Color layerColor = new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int) (alpha * 255));
            context.fill(x - expand, y - expand, x + width + expand, y + height + expand, layerColor.getRGB());
        }
    }

    public static void renderRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, Color color) {
        context.fill(x + radius, y, x + width - radius, y + height, color.getRGB());
        context.fill(x, y + radius, x + width, y + height - radius, color.getRGB());

        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                double distance = Math.sqrt(i * i + j * j);
                if (distance <= radius) {
                    context.fill(x + radius - i - 1, y + radius - j - 1, x + radius - i, y + radius - j, color.getRGB());
                    context.fill(x + width - radius + i, y + radius - j - 1, x + width - radius + i + 1, y + radius - j, color.getRGB());
                    context.fill(x + radius - i - 1, y + height - radius + j, x + radius - i, y + height - radius + j + 1, color.getRGB());
                    context.fill(x + width - radius + i, y + height - radius + j, x + width - radius + i + 1, y + height - radius + j + 1, color.getRGB());
                }
            }
        }
    }

    public static void renderDropdownArrow(DrawContext context, int x, int y, boolean expanded, Color color) {
        int arrowSize = 8;
        int halfSize = arrowSize / 2;

        if (expanded) {
            context.fill(x - halfSize, y - 2, x + halfSize, y - 1, color.getRGB());
            context.fill(x - halfSize + 1, y - 1, x + halfSize - 1, y, color.getRGB());
            context.fill(x - halfSize + 2, y, x + halfSize - 2, y + 1, color.getRGB());
            context.fill(x - 1, y + 1, x + 1, y + 2, color.getRGB());
        } else {
            context.fill(x - 2, y - halfSize, x - 1, y + halfSize, color.getRGB());
            context.fill(x - 1, y - halfSize + 1, x, y + halfSize - 1, color.getRGB());
            context.fill(x, y - halfSize + 2, x + 1, y + halfSize - 2, color.getRGB());
            context.fill(x + 1, y - 1, x + 2, y + 1, color.getRGB());
        }
    }

    public static void renderSlider(DrawContext context, int x, int y, int width, int height, double normalized, Color trackColor, Color fillColor) {
        renderGlow(context, x, y, width, height, fillColor, 0.02f);

        context.fill(x, y, x + width, y + height, trackColor.getRGB());

        float clamped = (float) Math.max(0.0, Math.min(1.0, normalized));
        int fillWidth = (int) (width * clamped);
        if (fillWidth > 0) {
            context.fill(x, y, x + fillWidth, y + height, fillColor.getRGB());
        }
    }

    public static void renderCheckbox(DrawContext context, int x, int y, int size, boolean enabled, Color borderColor, Color fillColor) {
        if (enabled) {
            renderGlow(context, x, y, size, size, borderColor, 0.02f);
        }

        context.fill(x, y, x + size, y + size, fillColor.getRGB());

        context.drawBorder(x, y, size, size, borderColor.getRGB());
    }

    public static Color getCategoryColor(com.volt.module.Category category) {
        return switch (category) {
            case COMBAT -> new Color(255, 100, 100);
            case MOVEMENT -> new Color(100, 255, 100);
            case PLAYER -> new Color(150, 100, 255);
            case RENDER -> new Color(255, 150, 100);
            case MISC -> new Color(200, 100, 255);
            case CLIENT -> new Color(150, 150, 150);
            case CONFIG -> new Color(100, 200, 255);
        };
    }
}