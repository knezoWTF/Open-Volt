package com.volt.utils.render;

import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public final class HudComponentUtils {

    public static void drawLineGraph(DrawContext context, int x, int y, int width, int height,
                                     List<Float> data, Color lineColor, Color fillColor, String title) {
        if (data.isEmpty()) return;

        RenderUtils.drawSmoothRoundedRect(context, x, y, width, height, 6,
                GuiUtils.Colors.BACKGROUND_DARK.getRGB());

        if (title != null && !title.isEmpty()) {
            GuiUtils.drawText(context, title, x + 8, y + 4, GuiUtils.Colors.TEXT_PRIMARY);
            y += 16;
            height -= 16;
        }

        if (data.size() == 1) {
            float value = data.get(0);
            int pointX = x + width / 2;
            int pointY = y + height / 2;

            RenderUtils.drawFilledCircle(context, pointX, pointY, 4, lineColor.getRGB());

            GuiUtils.drawCenteredText(context, String.format("%.1f", value), pointX, pointY - 15,
                    GuiUtils.Colors.TEXT_PRIMARY);
            return;
        }

        float min = Collections.min(data);
        float max = Collections.max(data);
        float range = max - min;
        if (range == 0) range = 1;

        Color gridColor = GuiUtils.withAlpha(GuiUtils.Colors.BORDER_LIGHT, 60);
        for (int i = 1; i < 5; i++) {
            int gridY = y + (height * i) / 5;
            context.fill(x + 4, gridY, x + width - 4, gridY + 1, gridColor.getRGB());
        }

        int[] xPoints = new int[data.size()];
        int[] yPoints = new int[data.size()];

        for (int i = 0; i < data.size(); i++) {
            xPoints[i] = x + 4 + (i * (width - 8)) / (data.size() - 1);
            yPoints[i] = y + height - 4 - (int) (((data.get(i) - min) / range) * (height - 8));
        }

        if (fillColor != null) {
            for (int i = 0; i < data.size() - 1; i++) {
                int x1 = xPoints[i];
                int y1 = yPoints[i];
                int x2 = xPoints[i + 1];
                int y2 = yPoints[i + 1];

                drawTrapezoid(context, x1, y1, x2, y2, y + height - 4, fillColor);
            }
        }

        for (int i = 0; i < data.size() - 1; i++) {
            drawLine(context, xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1],
                    2, lineColor);
        }

        for (int i = 0; i < data.size(); i++) {
            RenderUtils.drawFilledCircle(context, xPoints[i], yPoints[i], 3, lineColor.getRGB());
        }

        GuiUtils.drawText(context, String.format("%.1f", max), x + width - 30, y + 4,
                GuiUtils.Colors.TEXT_SECONDARY);
        GuiUtils.drawText(context, String.format("%.1f", min), x + width - 30, y + height - 12,
                GuiUtils.Colors.TEXT_SECONDARY);
    }

    public static void drawBarGraph(DrawContext context, int x, int y, int width, int height,
                                    List<Float> data, List<String> labels, Color barColor, String title) {
        if (data.isEmpty()) return;

        RenderUtils.drawSmoothRoundedRect(context, x, y, width, height, 6,
                GuiUtils.Colors.BACKGROUND_DARK.getRGB());

        if (title != null && !title.isEmpty()) {
            GuiUtils.drawText(context, title, x + 8, y + 4, GuiUtils.Colors.TEXT_PRIMARY);
            y += 16;
            height -= 16;
        }

        float max = Collections.max(data);
        if (max == 0) max = 1;

        int barWidth = (width - 16) / data.size() - 4;
        int startX = x + 8;

        for (int i = 0; i < data.size(); i++) {
            float value = data.get(i);
            int barHeight = (int) ((value / max) * (height - 24));
            int barX = startX + i * (barWidth + 4);
            int barY = y + height - 20 - barHeight;

            Color currentBarColor = GuiUtils.interpolateColor(barColor, barColor.brighter(),
                    value / max);
            RenderUtils.drawSmoothRoundedRect(context, barX, barY, barWidth, barHeight, 3,
                    currentBarColor.getRGB());

            GuiUtils.drawCenteredText(context, String.format("%.0f", value),
                    barX + barWidth / 2, barY - 12, GuiUtils.Colors.TEXT_SECONDARY);

            if (labels != null && i < labels.size()) {
                GuiUtils.drawCenteredText(context, labels.get(i), barX + barWidth / 2,
                        y + height - 12, GuiUtils.Colors.TEXT_SECONDARY);
            }
        }
    }

    public static void drawRadialMeter(DrawContext context, int centerX, int centerY, int radius,
                                       float value, float maxValue, Color meterColor, String label) {
        value = Math.max(0, Math.min(maxValue, value));
        float percentage = value / maxValue;

        RenderUtils.drawFilledCircle(context, centerX, centerY, radius,
                GuiUtils.Colors.BACKGROUND_DARK.getRGB());

        RenderUtils.drawFilledCircle(context, centerX, centerY, radius - 6,
                GuiUtils.Colors.BACKGROUND_LIGHT.getRGB());

        drawArc(context, centerX, centerY, radius - 3, -90, -90 + (int) (270 * percentage),
                6, meterColor);

        String valueText = String.format("%.0f", value);
        GuiUtils.drawCenteredText(context, valueText, centerX, centerY - 4,
                GuiUtils.Colors.TEXT_PRIMARY);

        if (label != null && !label.isEmpty()) {
            GuiUtils.drawCenteredText(context, label, centerX, centerY + 8,
                    GuiUtils.Colors.TEXT_SECONDARY);
        }

        String percentText = String.format("%.0f%%", percentage * 100);
        GuiUtils.drawCenteredText(context, percentText, centerX, centerY + 20,
                GuiUtils.Colors.TEXT_SECONDARY);
    }

    public static void drawStatusBar(DrawContext context, int x, int y, int width, int height,
                                     String label, float current, float max, Color barColor) {
        RenderUtils.drawSmoothRoundedRect(context, x, y, width, height, 4,
                GuiUtils.Colors.BACKGROUND_DARK.getRGB());

        GuiUtils.drawText(context, label, x + 6, y + 4, GuiUtils.Colors.TEXT_PRIMARY);

        String valueText = String.format("%.0f/%.0f", current, max);
        int textWidth = GuiUtils.getTextWidth(valueText);
        GuiUtils.drawText(context, valueText, x + width - textWidth - 6, y + 4,
                GuiUtils.Colors.TEXT_SECONDARY);

        int barY = y + 16;
        int barHeight = height - 20;
        float percentage = Math.max(0, Math.min(1, current / max));

        RenderUtils.drawSmoothRoundedRect(context, x + 6, barY, width - 12, barHeight, 2,
                GuiUtils.Colors.BORDER_DARK.getRGB());

        if (percentage > 0) {
            int fillWidth = (int) ((width - 12) * percentage);
            Color fillColor = percentage < 0.25f ? GuiUtils.Colors.ERROR :
                    percentage < 0.5f ? GuiUtils.Colors.WARNING :
                            barColor;
            RenderUtils.drawSmoothRoundedRect(context, x + 6, barY, fillWidth, barHeight, 2,
                    fillColor.getRGB());
        }
    }

    public static void drawInfoPanel(DrawContext context, int x, int y, int width, int height,
                                     String title, List<InfoItem> items) {
        RenderUtils.drawSmoothRoundedRect(context, x, y, width, height, 6,
                GuiUtils.Colors.BACKGROUND_DARK.getRGB());

        GuiUtils.drawText(context, title, x + 8, y + 8, GuiUtils.Colors.TEXT_PRIMARY);

        context.fill(x + 8, y + 22, x + width - 8, y + 23,
                GuiUtils.Colors.BORDER_LIGHT.getRGB());

        int currentY = y + 30;
        for (InfoItem item : items) {
            GuiUtils.drawText(context, item.key, x + 8, currentY, GuiUtils.Colors.TEXT_SECONDARY);

            int valueWidth = GuiUtils.getTextWidth(item.value);
            GuiUtils.drawText(context, item.value, x + width - valueWidth - 8, currentY,
                    item.color != null ? item.color : GuiUtils.Colors.TEXT_PRIMARY);

            currentY += 14;
        }
    }

    public static void drawMiniMap(DrawContext context, int x, int y, int size,
                                   List<MapPoint> points, Color backgroundColor) {
        RenderUtils.drawSmoothRoundedRect(context, x, y, size, size, 6, backgroundColor.getRGB());

        GuiUtils.drawBorder(context, x, y, size, size, 6, 1, GuiUtils.Colors.BORDER_LIGHT);

        int centerX = x + size / 2;
        int centerY = y + size / 2;
        context.fill(centerX - 4, centerY, centerX + 4, centerY + 1,
                GuiUtils.Colors.TEXT_PRIMARY.getRGB());
        context.fill(centerX, centerY - 4, centerX + 1, centerY + 4,
                GuiUtils.Colors.TEXT_PRIMARY.getRGB());

        for (MapPoint point : points) {
            int pointX = centerX + (int) (point.relativeX * size / 2);
            int pointY = centerY + (int) (point.relativeZ * size / 2);

            if (pointX >= x && pointX <= x + size && pointY >= y && pointY <= y + size) {
                RenderUtils.drawFilledCircle(context, pointX, pointY, point.size, point.color.getRGB());
            }
        }
    }

    private static void drawTrapezoid(DrawContext context, int x1, int y1, int x2, int y2,
                                      int baseY, Color color) {
        int steps = Math.abs(x2 - x1);
        if (steps == 0) return;

        for (int i = 0; i <= steps; i++) {
            float progress = (float) i / steps;
            int currentX = x1 + (int) ((x2 - x1) * progress);
            int currentY = y1 + (int) ((y2 - y1) * progress);

            context.fill(currentX, currentY, currentX + 1, baseY, color.getRGB());
        }
    }

    private static void drawLine(DrawContext context, int x1, int y1, int x2, int y2,
                                 int thickness, Color color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1;
        int y = y1;

        for (; ; ) {
            for (int i = -thickness / 2; i <= thickness / 2; i++) {
                for (int j = -thickness / 2; j <= thickness / 2; j++) {
                    context.fill(x + i, y + j, x + i + 1, y + j + 1, color.getRGB());
                }
            }

            if (x == x2 && y == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
    }

    private static void drawArc(DrawContext context, int centerX, int centerY, int radius,
                                int startAngle, int endAngle, int thickness, Color color) {
        for (int angle = startAngle; angle <= endAngle; angle += 2) {
            double radians = Math.toRadians(angle);
            int x = centerX + (int) (radius * Math.cos(radians));
            int y = centerY + (int) (radius * Math.sin(radians));

            for (int i = -thickness / 2; i <= thickness / 2; i++) {
                for (int j = -thickness / 2; j <= thickness / 2; j++) {
                    if (i * i + j * j <= (thickness / 2) * (thickness / 2)) {
                        context.fill(x + i, y + j, x + i + 1, y + j + 1, color.getRGB());
                    }
                }
            }
        }
    }

    public record InfoItem(String key, String value, Color color) {
            public InfoItem(String key, String value) {
                this(key, value, null);
            }

    }

    public record MapPoint(float relativeX, float relativeZ, Color color, int size, String label) {
    }

    public static class DataBuffer {
        private final List<Float> data;
        private final int maxSize;

        public DataBuffer(int maxSize) {
            this.maxSize = maxSize;
            this.data = new ArrayList<>();
        }

        public void addValue(float value) {
            data.add(value);
            if (data.size() > maxSize) {
                data.remove(0);
            }
        }

        public List<Float> getData() {
            return new ArrayList<>(data);
        }

        public float getAverage() {
            if (data.isEmpty()) return 0;
            return (float) data.stream().mapToDouble(Float::doubleValue).average().orElse(0);
        }

        public float getMax() {
            return data.isEmpty() ? 0 : Collections.max(data);
        }

        public float getMin() {
            return data.isEmpty() ? 0 : Collections.min(data);
        }

        public void clear() {
            data.clear();
        }
    }
} 