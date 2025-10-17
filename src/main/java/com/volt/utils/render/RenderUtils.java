package com.volt.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.volt.Volt;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

@UtilityClass
public final class RenderUtils {
    public static VertexSorter vertexSorter;
    public static boolean rendering3D = true;

    public static void unscaledProjection() {
        vertexSorter = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, Volt.mc.getWindow().getFramebufferWidth(), Volt.mc.getWindow().getFramebufferHeight(), 0, 1000, 21000), VertexSorter.BY_Z);
        rendering3D = false;
    }

    public static void scaledProjection() {
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, (float) (Volt.mc.getWindow().getFramebufferWidth() / Volt.mc.getWindow().getScaleFactor()), (float) (Volt.mc.getWindow().getFramebufferHeight() / Volt.mc.getWindow().getScaleFactor()), 0, 1000, 21000), vertexSorter);
        rendering3D = true;
    }

    public static void renderOutline(MatrixStack matrices, Box box, Color color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);

        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public static void renderFilled(MatrixStack matrices, Box box, Color color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);

        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);

        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public static void renderLine(MatrixStack matrices, Vec3d start, Vec3d end, Color color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = color.getAlpha() / 255.0f;

        buffer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z).color(r, g, b, a);
        buffer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }


    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {

        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);


        drawRoundedCorner(context, x + radius, y + radius, radius, color, 0);
        drawRoundedCorner(context, x + width - radius, y + radius, radius, color, 1);
        drawRoundedCorner(context, x + radius, y + height - radius, radius, color, 2);
        drawRoundedCorner(context, x + width - radius, y + height - radius, radius, color, 3);
    }

    public static void drawRoundedRectGradient(DrawContext context, int x, int y, int width, int height, int radius, int colorTop, int colorBottom) {
        context.fillGradient(x + radius, y, x + width - radius, y + height, colorTop, colorBottom);
        context.fillGradient(x, y + radius, x + radius, y + height - radius, colorTop, colorBottom);
        context.fillGradient(x + width - radius, y + radius, x + width, y + height - radius, colorTop, colorBottom);

        drawRoundedCornerGradient(context, x + radius, y + radius, radius, colorTop, colorBottom, 0);
        drawRoundedCornerGradient(context, x + width - radius, y + radius, radius, colorTop, colorBottom, 1);
        drawRoundedCornerGradient(context, x + radius, y + height - radius, radius, colorTop, colorBottom, 2);
        drawRoundedCornerGradient(context, x + width - radius, y + height - radius, radius, colorTop, colorBottom, 3);
    }

    private static void drawRoundedCorner(DrawContext context, int centerX, int centerY, int radius, int color, int corner) {
        float radiusF = (float) radius;

        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);

                if (distance <= radiusF) {
                    int pixelX = centerX;
                    int pixelY = centerY;

                    switch (corner) {
                        case 0:
                            pixelX = centerX - i;
                            pixelY = centerY - j;
                            break;
                        case 1:
                            pixelX = centerX + i;
                            pixelY = centerY - j;
                            break;
                        case 2:
                            pixelX = centerX - i;
                            pixelY = centerY + j;
                            break;
                        case 3:
                            pixelX = centerX + i;
                            pixelY = centerY + j;
                            break;
                    }

                    float alpha = 1.0f;
                    if (distance > radiusF - 1.0f) {
                        alpha = Math.max(0.0f, radiusF - distance);
                    }

                    int originalAlpha = (color >> 24) & 0xFF;
                    int newAlpha = (int) (originalAlpha * alpha);
                    int antiAliasedColor = (newAlpha << 24) | (color & 0x00FFFFFF);

                    context.fill(pixelX, pixelY, pixelX + 1, pixelY + 1, antiAliasedColor);
                }
            }
        }
    }

    public static void drawFilledCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    context.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
                }
            }
        }
    }


    private static void drawRoundedCornerGradient(DrawContext context, int centerX, int centerY, int radius, int colorTop, int colorBottom, int corner) {
        float radiusF = (float) radius;

        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                float distance = (float) Math.sqrt(i * i + j * j);

                if (distance <= radiusF) {
                    int pixelX = centerX;
                    int pixelY = centerY;

                    switch (corner) {
                        case 0:
                            pixelX = centerX - i;
                            pixelY = centerY - j;
                            break;
                        case 1:
                            pixelX = centerX + i;
                            pixelY = centerY - j;
                            break;
                        case 2:
                            pixelX = centerX - i;
                            pixelY = centerY + j;
                            break;
                        case 3:
                            pixelX = centerX + i;
                            pixelY = centerY + j;
                            break;
                    }

                    float gradientFactor = (float) j / radius;
                    int interpolatedColor = interpolateColor(colorTop, colorBottom, gradientFactor);

                    float alpha = 1.0f;
                    if (distance > radiusF - 1.0f) {
                        alpha = Math.max(0.0f, radiusF - distance);
                    }

                    int originalAlpha = (interpolatedColor >> 24) & 0xFF;
                    int newAlpha = (int) (originalAlpha * alpha);
                    int antiAliasedColor = (newAlpha << 24) | (interpolatedColor & 0x00FFFFFF);

                    context.fill(pixelX, pixelY, pixelX + 1, pixelY + 1, antiAliasedColor);
                }
            }
        }
    }

    private static int interpolateColor(int color1, int color2, float factor) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * factor);
        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static void drawGlow(DrawContext context, int x, int y, int width, int height, int radius, int color, int glowRadius) {
        for (int i = 1; i <= glowRadius; i++) {
            int alpha = (int) (((color >> 24) & 0xFF) * (1.0f - (float) i / glowRadius) * 0.3f);
            int glowColor = (alpha << 24) | (color & 0x00FFFFFF);

            drawRoundedRect(context, x - i, y - i, width + (i * 2), height + (i * 2), radius + i, glowColor);
        }
    }

    public static void drawSmoothRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);

        int edgeColor = (color & 0x00FFFFFF) | (((color >> 24) & 0xFF) / 2 << 24);
        context.fill(x - 1, y, x, y + height, edgeColor);
        context.fill(x + width, y, x + width + 1, y + height, edgeColor);
        context.fill(x, y - 1, x + width, y, edgeColor);
        context.fill(x, y + height, x + width, y + height + 1, edgeColor);
    }

    public static void drawSmoothRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        drawSmoothRoundedCorner(context, x + radius, y + radius, radius, color, 0);
        drawSmoothRoundedCorner(context, x + width - radius, y + radius, radius, color, 1);
        drawSmoothRoundedCorner(context, x + radius, y + height - radius, radius, color, 2);
        drawSmoothRoundedCorner(context, x + width - radius, y + height - radius, radius, color, 3);
    }

    private static void drawSmoothRoundedCorner(DrawContext context, int centerX, int centerY, int radius, int color, int corner) {
        float radiusF = (float) radius;

        for (float i = 0; i < radius; i += 0.5f) {
            for (float j = 0; j < radius; j += 0.5f) {
                float distance = (float) Math.sqrt(i * i + j * j);

                if (distance <= radiusF) {
                    int pixelX = centerX;
                    int pixelY = centerY;

                    switch (corner) {
                        case 0:
                            pixelX = centerX - (int) i;
                            pixelY = centerY - (int) j;
                            break;
                        case 1:
                            pixelX = centerX + (int) i;
                            pixelY = centerY - (int) j;
                            break;
                        case 2:
                            pixelX = centerX - (int) i;
                            pixelY = centerY + (int) j;
                            break;
                        case 3:
                            pixelX = centerX + (int) i;
                            pixelY = centerY + (int) j;
                            break;
                    }

                    float coverage = calculateCoverage(i, j, radiusF);

                    if (coverage > 0) {
                        int originalAlpha = (color >> 24) & 0xFF;
                        int newAlpha = (int) (originalAlpha * coverage);
                        int antiAliasedColor = (newAlpha << 24) | (color & 0x00FFFFFF);

                        context.fill(pixelX, pixelY, pixelX + 1, pixelY + 1, antiAliasedColor);
                    }
                }
            }
        }
    }

    private static float calculateCoverage(float x, float y, float radius) {
        float distance = (float) Math.sqrt(x * x + y * y);

        if (distance <= radius - 1.0f) {
            return 1.0f;
        } else if (distance >= radius) {
            return 0.0f;
        } else {
            return Math.max(0.0f, radius - distance);
        }
    }

    public static void drawGradientRect(DrawContext context, int x, int y, int width, int height, int colorTop, int colorBottom) {
        context.fillGradient(x, y, x + width, y + height, colorTop, colorBottom);
    }

    public static void fillWithGlow(DrawContext context, int x1, int y1, int x2, int y2, int color, float glowIntensity) {
        Color c = new Color(color, true);

        for (int pass = 0; pass < 4; pass++) {
            int glowExpand = 0;
            int glowAlpha = 0;

            switch (pass) {
                case 0:
                    glowExpand = 3;
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.15f);
                    break;
                case 1:
                    glowExpand = 2;
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.3f);
                    break;
                case 2:
                    glowExpand = 1;
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.5f);
                    break;
                case 3:
                    glowExpand = 0;
                    glowAlpha = c.getAlpha();
                    break;
            }

            int glowColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), glowAlpha).getRGB();
            context.fill(x1 - glowExpand, y1 - glowExpand, x2 + glowExpand, y2 + glowExpand, glowColor);
        }
    }

    public static void drawTextWithGlow(DrawContext context, net.minecraft.client.font.TextRenderer textRenderer,
                                        String text, int x, int y, int color, float glowIntensity) {
        Color c = new Color(color, true);

        for (int pass = 0; pass < 4; pass++) {
            int glowAlpha = 0;
            int offsetX = 0;
            int offsetY = 0;

            switch (pass) {
                case 0:
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.2f);
                    offsetX = 1;
                    offsetY = 1;
                    break;
                case 1:
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.3f);
                    offsetX = 1;
                    offsetY = 0;
                    break;
                case 2:
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.3f);
                    offsetX = 0;
                    offsetY = 1;
                    break;
                case 3:
                    glowAlpha = c.getAlpha();
                    offsetX = 0;
                    offsetY = 0;
                    break;
            }

            int glowColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), glowAlpha).getRGB();
            context.drawText(textRenderer, text, x + offsetX, y + offsetY, glowColor, false);
        }
    }

    public static void drawCustomTextWithGlow(MatrixStack matrices, com.volt.utils.render.font.fonts.FontRenderer fontRenderer,
                                              String text, int x, int y, int color, float glowIntensity) {
        Color c = new Color(color, true);

        for (int pass = 0; pass < 4; pass++) {
            int glowAlpha = 0;
            int offsetX = 0;
            int offsetY = 0;

            switch (pass) {
                case 0:
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.2f);
                    offsetX = 1;
                    offsetY = 1;
                    break;
                case 1:
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.3f);
                    offsetX = 1;
                    offsetY = 0;
                    break;
                case 2:
                    glowAlpha = (int) (c.getAlpha() * glowIntensity * 0.3f);
                    offsetX = 0;
                    offsetY = 1;
                    break;
                case 3:
                    glowAlpha = c.getAlpha();
                    offsetX = 0;
                    offsetY = 0;
                    break;
            }

            int glowColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), glowAlpha).getRGB();
            fontRenderer.drawString(matrices, text, x + offsetX, y + offsetY, new Color(glowColor, true));
        }
    }
}