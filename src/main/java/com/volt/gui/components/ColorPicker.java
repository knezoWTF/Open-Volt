package com.volt.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.module.setting.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

import java.awt.*;

public class ColorPicker {
    private static final int SIZE = 200, BAR_WIDTH = 20, SPACING = 10, PREVIEW_SIZE = 40;
    private static final int COMPACT_SIZE = 120, COMPACT_BAR_WIDTH = 15, COMPACT_SPACING = 8;
    private static final int CONTROL_HEIGHT = 18;
    private static final int CONTROL_SPACING = 8;

    private final ColorSetting colorSetting;
    private boolean isDraggingSquare, isDraggingBrightness, isDraggingAlpha;
    private boolean draggingR, draggingG, draggingB, draggingA;
    private boolean focusHex, focusR, focusG, focusB, focusA;
    private String inputHex = "";
    private String inputR = "";
    private String inputG = "";
    private String inputB = "";
    private String inputA = "";
    private boolean expanded = false;
    private int posX, posY;

    private int hexX, hexY, hexW, hexH;
    private int rSliderX, rSliderY, rSliderW, rSliderH, rBoxX, rBoxY, rBoxW, rBoxH;
    private int gSliderX, gSliderY, gSliderW, gSliderH, gBoxX, gBoxY, gBoxW, gBoxH;
    private int bSliderX, bSliderY, bSliderW, bSliderH, bBoxX, bBoxY, bBoxW, bBoxH;
    private int aSliderX, aSliderY, aSliderW, aSliderH, aBoxX, aBoxY, aBoxW, aBoxH;

    public ColorPicker(ColorSetting colorSetting) {
        this.colorSetting = colorSetting;
    }

    public ColorPicker() {
        this.colorSetting = null;
    }

    public void render(DrawContext context, int x, int y, int mouseX, int mouseY) {
        this.posX = x;
        this.posY = y;

        renderColorSquare(context, x, y);

        int brightnessX = x + SIZE + SPACING;
        renderGradientBar(context, brightnessX, y, SIZE, true);

        if (colorSetting.isHasAlpha()) {
            int alphaX = brightnessX + BAR_WIDTH + SPACING;
            renderGradientBar(context, alphaX, y, SIZE, false);
        }

        renderSelectionIndicators(context, x, y);

        int previewInset = 6;
        int previewX = x + SIZE - PREVIEW_SIZE - previewInset;
        int previewY = y + SIZE - PREVIEW_SIZE - previewInset;
        renderColorPreview(context, previewX, previewY);


        renderControls(context, x, y + SIZE + 10, mouseX, mouseY);
    }

    private void renderColorSquare(DrawContext context, int x, int y) {
        setupRendering(context);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float brightness = colorSetting.getHSB()[2];
        int steps = 50;

        for (int i = 0; i < steps; i++) {
            for (int j = 0; j < steps; j++) {
                float[] hues = {(float) i / steps, (float) (i + 1) / steps};
                float[] sats = {(float) j / steps, (float) (j + 1) / steps};

                Color[] colors = {
                        Color.getHSBColor(hues[0], sats[0], brightness),
                        Color.getHSBColor(hues[1], sats[0], brightness),
                        Color.getHSBColor(hues[1], sats[1], brightness),
                        Color.getHSBColor(hues[0], sats[1], brightness)
                };

                float[] xs = {x + i * SIZE / steps, x + (i + 1) * SIZE / steps};
                float[] ys = {y + j * SIZE / steps, y + (j + 1) * SIZE / steps};

                addQuad(buffer, matrix, xs, ys, colors);
            }
        }

        finishRendering(buffer, context, x, y, SIZE, SIZE);
    }

    private void renderGradientBar(DrawContext context, int x, int y, int height, boolean isBrightness) {
        if (!isBrightness) renderCheckerboard(context, x, y, BAR_WIDTH, height);

        setupRendering(context);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float[] hsb = colorSetting.getHSB();
        Color baseColor = colorSetting.getValue();

        for (int i = 0; i < 20; i++) {
            float t1 = (float) i / 20, t2 = (float) (i + 1) / 20;
            float y1 = y + t1 * height, y2 = y + t2 * height;

            Color color1, color2;
            if (isBrightness) {
                color1 = Color.getHSBColor(hsb[0], hsb[1], 1 - t1);
                color2 = Color.getHSBColor(hsb[0], hsb[1], 1 - t2);
            } else {
                int alpha1 = (int) ((1 - t1) * 255), alpha2 = (int) ((1 - t2) * 255);
                color1 = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha1);
                color2 = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha2);
            }

            addRect(buffer, matrix, x, y1, x + BAR_WIDTH, y2, color1, color2);
        }

        finishRendering(buffer, context, x, y, BAR_WIDTH, height);
    }

    private void renderColorPreview(DrawContext context, int x, int y) {
        if (colorSetting.isHasAlpha()) renderCheckerboard(context, x, y, PREVIEW_SIZE, PREVIEW_SIZE);

        Color color = colorSetting.getValue();
        context.fill(x, y, x + PREVIEW_SIZE, y + PREVIEW_SIZE, color.getRGB());
        context.drawBorder(x, y, PREVIEW_SIZE, PREVIEW_SIZE, new Color(100, 100, 100).getRGB());
    }

    private void renderSelectionIndicators(DrawContext context, int x, int y) {
        float[] hsb = colorSetting.getHSB();

        int squareX = (int) (x + hsb[0] * SIZE), squareY = (int) (y + hsb[1] * SIZE);
        drawCrosshair(context, squareX, squareY);

        int brightnessX = x + SIZE + SPACING;
        int brightnessY = (int) (y + (1 - hsb[2]) * SIZE);
        drawBarIndicator(context, brightnessX, brightnessY, BAR_WIDTH);

        if (colorSetting.isHasAlpha()) {
            int alphaX = brightnessX + BAR_WIDTH + SPACING;
            int alphaY = (int) (y + (1 - colorSetting.getAlpha() / 255f) * SIZE);
            drawBarIndicator(context, alphaX, alphaY, BAR_WIDTH);
        }
    }

    private void renderCheckerboard(DrawContext context, int x, int y, int width, int height) {
        for (int i = 0; i < width; i += 8) {
            for (int j = 0; j < height; j += 8) {
                boolean light = ((i / 8) + (j / 8)) % 2 == 0;
                Color color = light ? new Color(200, 200, 200) : new Color(150, 150, 150);
                context.fill(x + i, y + j, x + i + Math.min(8, width - i), y + j + Math.min(8, height - j), color.getRGB());
            }
        }
    }


    private void setupRendering(DrawContext context) {
        context.getMatrices().push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    }

    private void finishRendering(BufferBuilder buffer, DrawContext context, int x, int y, int width, int height) {
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
        context.getMatrices().pop();
        context.drawBorder(x, y, width, height, new Color(100, 100, 100).getRGB());
    }

    private void addQuad(BufferBuilder buffer, Matrix4f matrix, float[] xs, float[] ys, Color[] colors) {
        buffer.vertex(matrix, xs[0], ys[0], 0).color(colors[0].getRed(), colors[0].getGreen(), colors[0].getBlue(), 255);
        buffer.vertex(matrix, xs[1], ys[0], 0).color(colors[1].getRed(), colors[1].getGreen(), colors[1].getBlue(), 255);
        buffer.vertex(matrix, xs[1], ys[1], 0).color(colors[2].getRed(), colors[2].getGreen(), colors[2].getBlue(), 255);
        buffer.vertex(matrix, xs[0], ys[1], 0).color(colors[3].getRed(), colors[3].getGreen(), colors[3].getBlue(), 255);
    }

    private void addRect(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, Color color1, Color color2) {
        buffer.vertex(matrix, x1, y1, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        buffer.vertex(matrix, x2, y1, 0).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha());
        buffer.vertex(matrix, x2, y2, 0).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
        buffer.vertex(matrix, x1, y2, 0).color(color2.getRed(), color2.getGreen(), color2.getBlue(), color2.getAlpha());
    }

    private void drawCrosshair(DrawContext context, int x, int y) {
        context.fill(x - 5, y - 1, x + 5, y + 1, Color.WHITE.getRGB());
        context.fill(x - 1, y - 5, x + 1, y + 5, Color.WHITE.getRGB());
        context.fill(x - 6, y - 2, x + 6, y + 2, Color.BLACK.getRGB());
        context.fill(x - 2, y - 6, x + 2, y + 6, Color.BLACK.getRGB());
    }

    private void drawBarIndicator(DrawContext context, int x, int y, int width) {
        context.fill(x - 2, y - 1, x + width + 2, y + 1, Color.WHITE.getRGB());
        context.fill(x - 3, y - 2, x + width + 3, y + 2, Color.BLACK.getRGB());
    }


    public void setPosition(int x, int y) {
        this.posX = x;
        this.posY = y;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || colorSetting == null) return false;

        int size = SIZE;
        int barWidth = BAR_WIDTH;
        int spacing = SPACING;

        if (isInBounds(mouseX, mouseY, posX, posY, size, size)) {
            isDraggingSquare = true;
            updateSquareColor(mouseX, mouseY);
            return true;
        }

        int brightnessX = posX + size + spacing;
        if (isInBounds(mouseX, mouseY, brightnessX, posY, barWidth, size)) {
            isDraggingBrightness = true;
            updateBrightness(mouseY);
            return true;
        }

        if (colorSetting.isHasAlpha()) {
            int alphaX = brightnessX + barWidth + spacing;
            if (isInBounds(mouseX, mouseY, alphaX, posY, barWidth, size)) {
                isDraggingAlpha = true;
                updateAlpha(mouseY);
                return true;
            }
        }


        if (isInBounds(mouseX, mouseY, hexX, hexY, hexW, hexH)) {
            setFocus(true, false, false, false, false);
            return true;
        }
        if (isInBounds(mouseX, mouseY, rSliderX, rSliderY, rSliderW, rSliderH)) {
            draggingR = true;
            updateRFromMouse(mouseX);
            return true;
        }
        if (isInBounds(mouseX, mouseY, rBoxX, rBoxY, rBoxW, rBoxH)) {
            setFocus(false, true, false, false, false);
            return true;
        }
        if (isInBounds(mouseX, mouseY, gSliderX, gSliderY, gSliderW, gSliderH)) {
            draggingG = true;
            updateGFromMouse(mouseX);
            return true;
        }
        if (isInBounds(mouseX, mouseY, gBoxX, gBoxY, gBoxW, gBoxH)) {
            setFocus(false, false, true, false, false);
            return true;
        }
        if (isInBounds(mouseX, mouseY, bSliderX, bSliderY, bSliderW, bSliderH)) {
            draggingB = true;
            updateBFromMouse(mouseX);
            return true;
        }
        if (isInBounds(mouseX, mouseY, bBoxX, bBoxY, bBoxW, bBoxH)) {
            setFocus(false, false, false, true, false);
            return true;
        }
        if (colorSetting.isHasAlpha()) {
            if (isInBounds(mouseX, mouseY, aSliderX, aSliderY, aSliderW, aSliderH)) {
                draggingA = true;
                updateAFromMouse(mouseX);
                return true;
            }
            if (isInBounds(mouseX, mouseY, aBoxX, aBoxY, aBoxW, aBoxH)) {
                setFocus(false, false, false, false, true);
                return true;
            }
        }

        setFocus(false, false, false, false, false);
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button != 0 || colorSetting == null) return false;

        if (isDraggingSquare) {
            updateSquareColor(mouseX, mouseY);
            return true;
        }
        if (isDraggingBrightness) {
            updateBrightness(mouseY);
            return true;
        }
        if (isDraggingAlpha) {
            updateAlpha(mouseY);
            return true;
        }
        if (draggingR) {
            updateRFromMouse(mouseX);
            return true;
        }
        if (draggingG) {
            updateGFromMouse(mouseX);
            return true;
        }
        if (draggingB) {
            updateBFromMouse(mouseX);
            return true;
        }
        if (draggingA) {
            updateAFromMouse(mouseX);
            return true;
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        boolean wasHandling = isDraggingSquare || isDraggingBrightness || isDraggingAlpha || draggingR || draggingG || draggingB || draggingA;
        isDraggingSquare = isDraggingBrightness = isDraggingAlpha = false;
        draggingR = draggingG = draggingB = draggingA = false;
        return wasHandling;
    }

    private boolean isInBounds(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void updateSquareColor(double mouseX, double mouseY) {
        float hue = (float) Math.max(0, Math.min(1, (mouseX - posX) / SIZE));
        float saturation = (float) Math.max(0, Math.min(1, (mouseY - posY) / SIZE));
        float[] hsb = colorSetting.getHSB();
        colorSetting.setFromHSB(hue, saturation, hsb[2]);
    }

    private void updateBrightness(double mouseY) {
        float brightness = 1 - (float) Math.max(0, Math.min(1, (mouseY - posY) / SIZE));
        float[] hsb = colorSetting.getHSB();
        colorSetting.setFromHSB(hsb[0], hsb[1], brightness);
    }

    private void updateAlpha(double mouseY) {
        float alpha = 1 - (float) Math.max(0, Math.min(1, (mouseY - posY) / SIZE));
        Color color = colorSetting.getValue();
        colorSetting.setValue(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255));
    }

    private void renderControls(DrawContext context, int x, int y, int mouseX, int mouseY) {
        updateInputsFromColor();

        int totalW = getWidth();
        int rowY = y;
        int textColor = new Color(200, 200, 200).getRGB();


        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "HEX", x, rowY + 3, textColor);
        hexX = x + 28;
        hexY = rowY;
        hexW = totalW - 28;
        hexH = CONTROL_HEIGHT;
        drawTextField(context, hexX, hexY, hexW, hexH, inputHex.isEmpty() ? colorToHexDisplay() : inputHex, focusHex);
        rowY += CONTROL_HEIGHT + CONTROL_SPACING;


        int boxW = 50;
        int boxH = CONTROL_HEIGHT;
        int sliderX = x + 28;
        int sliderW = totalW - 28 - boxW - 6;
        int sliderH = 8;


        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "R", x, rowY + 3, textColor);
        rSliderX = sliderX;
        rSliderY = rowY + (CONTROL_HEIGHT - sliderH) / 2;
        rSliderW = sliderW;
        rSliderH = sliderH;
        UIRenderer.renderSlider(context, rSliderX, rSliderY, rSliderW, rSliderH, colorSetting.getRed() / 255.0, new Color(60, 60, 60), new Color(255, 80, 80));
        rBoxX = sliderX + sliderW + 6;
        rBoxY = rowY;
        rBoxW = boxW;
        rBoxH = boxH;
        drawTextField(context, rBoxX, rBoxY, rBoxW, rBoxH, inputR.isEmpty() ? String.valueOf(colorSetting.getRed()) : inputR, focusR);
        rowY += CONTROL_HEIGHT + CONTROL_SPACING;


        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "G", x, rowY + 3, textColor);
        gSliderX = sliderX;
        gSliderY = rowY + (CONTROL_HEIGHT - sliderH) / 2;
        gSliderW = sliderW;
        gSliderH = sliderH;
        UIRenderer.renderSlider(context, gSliderX, gSliderY, gSliderW, gSliderH, colorSetting.getGreen() / 255.0, new Color(60, 60, 60), new Color(80, 255, 80));
        gBoxX = sliderX + sliderW + 6;
        gBoxY = rowY;
        gBoxW = boxW;
        gBoxH = boxH;
        drawTextField(context, gBoxX, gBoxY, gBoxW, gBoxH, inputG.isEmpty() ? String.valueOf(colorSetting.getGreen()) : inputG, focusG);
        rowY += CONTROL_HEIGHT + CONTROL_SPACING;


        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "B", x, rowY + 3, textColor);
        bSliderX = sliderX;
        bSliderY = rowY + (CONTROL_HEIGHT - sliderH) / 2;
        bSliderW = sliderW;
        bSliderH = sliderH;
        UIRenderer.renderSlider(context, bSliderX, bSliderY, bSliderW, bSliderH, colorSetting.getBlue() / 255.0, new Color(60, 60, 60), new Color(80, 80, 255));
        bBoxX = sliderX + sliderW + 6;
        bBoxY = rowY;
        bBoxW = boxW;
        bBoxH = boxH;
        drawTextField(context, bBoxX, bBoxY, bBoxW, bBoxH, inputB.isEmpty() ? String.valueOf(colorSetting.getBlue()) : inputB, focusB);
        rowY += CONTROL_HEIGHT + CONTROL_SPACING;

        if (colorSetting.isHasAlpha()) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "A", x, rowY + 3, textColor);
            aSliderX = sliderX;
            aSliderY = rowY + (CONTROL_HEIGHT - sliderH) / 2;
            aSliderW = sliderW;
            aSliderH = sliderH;
            UIRenderer.renderSlider(context, aSliderX, aSliderY, aSliderW, aSliderH, colorSetting.getAlpha() / 255.0, new Color(60, 60, 60), new Color(200, 200, 200));
            aBoxX = sliderX + sliderW + 6;
            aBoxY = rowY;
            aBoxW = boxW;
            aBoxH = boxH;
            drawTextField(context, aBoxX, aBoxY, aBoxW, aBoxH, inputA.isEmpty() ? String.valueOf(colorSetting.getAlpha()) : inputA, focusA);
        }
    }

    private void drawTextField(DrawContext ctx, int x, int y, int w, int h, String text, boolean focused) {
        Color bg = focused ? new Color(45, 45, 65, 240) : new Color(40, 40, 55, 220);
        ctx.fill(x, y, x + w, y + h, bg.getRGB());
        ctx.drawBorder(x, y, w, h, new Color(100, 100, 100).getRGB());
        int color = new Color(220, 220, 220).getRGB();
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x + 5, y + (h - 8) / 2, color);
    }

    private void updateInputsFromColor() {
        Color c = colorSetting.getValue();
        if (!focusR) inputR = String.valueOf(c.getRed());
        if (!focusG) inputG = String.valueOf(c.getGreen());
        if (!focusB) inputB = String.valueOf(c.getBlue());
        if (colorSetting.isHasAlpha() && !focusA) inputA = String.valueOf(c.getAlpha());
        if (!focusHex) inputHex = colorToHexDisplay();
    }

    private String colorToHexDisplay() {
        Color c = colorSetting.getValue();
        String base = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        if (colorSetting.isHasAlpha()) base += String.format("%02X", c.getAlpha());
        return base;
    }

    private String clampNumeric(String s) {
        if (s == null || s.isEmpty()) return "";
        try {
            int v = Integer.parseInt(s);
            v = Math.max(0, Math.min(255, v));
            return String.valueOf(v);
        } catch (NumberFormatException ignored) {
            return "";
        }
    }

    private void applyInputsIfValid() {
        try {
            if (focusHex) {
                String s = inputHex.trim();
                if (s.startsWith("#")) s = s.substring(1);
                if (s.length() == 6 || s.length() == 8) {
                    int r = Integer.parseInt(s.substring(0, 2), 16);
                    int g = Integer.parseInt(s.substring(2, 4), 16);
                    int b = Integer.parseInt(s.substring(4, 6), 16);
                    int a = colorSetting.isHasAlpha() ? (s.length() == 8 ? Integer.parseInt(s.substring(6, 8), 16) : colorSetting.getAlpha()) : 255;
                    if (colorSetting.isHasAlpha()) colorSetting.setValue(r, g, b, a);
                    else colorSetting.setValue(r, g, b);
                }
            } else {
                String rS = inputR, gS = inputG, bS = inputB, aS = inputA;
                if (!rS.isEmpty() || !gS.isEmpty() || !bS.isEmpty() || (colorSetting.isHasAlpha() && !aS.isEmpty())) {
                    int r = rS.isEmpty() ? colorSetting.getRed() : Integer.parseInt(clampNumeric(rS));
                    int g = gS.isEmpty() ? colorSetting.getGreen() : Integer.parseInt(clampNumeric(gS));
                    int b = bS.isEmpty() ? colorSetting.getBlue() : Integer.parseInt(clampNumeric(bS));
                    if (colorSetting.isHasAlpha()) {
                        int a = aS.isEmpty() ? colorSetting.getAlpha() : Integer.parseInt(clampNumeric(aS));
                        colorSetting.setValue(r, g, b, a);
                    } else {
                        colorSetting.setValue(r, g, b);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }


    public boolean keyPressed(int keyCode) {
        if (!(focusHex || focusR || focusG || focusB || focusA)) return false;

        if (keyCode == 259 /* GLFW_KEY_BACKSPACE */) {
            if (focusHex && !inputHex.isEmpty()) inputHex = inputHex.substring(0, inputHex.length() - 1);
            if (focusR && !inputR.isEmpty()) inputR = inputR.substring(0, inputR.length() - 1);
            if (focusG && !inputG.isEmpty()) inputG = inputG.substring(0, inputG.length() - 1);
            if (focusB && !inputB.isEmpty()) inputB = inputB.substring(0, inputB.length() - 1);
            if (focusA && !inputA.isEmpty()) inputA = inputA.substring(0, inputA.length() - 1);
            applyInputsIfValid();
            return true;
        }

        if (keyCode == 257 /* GLFW_KEY_ENTER */ || keyCode == 335 /* GLFW_KEY_KP_ENTER */) {
            applyInputsIfValid();
            setFocus(false, false, false, false, false);
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr) {
        if (focusHex) {
            if ((chr == '#') || (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'f') || (chr >= 'A' && chr <= 'F')) {
                inputHex += Character.toUpperCase(chr);
                if (inputHex.length() > 9) inputHex = inputHex.substring(0, 9);
                applyInputsIfValid();
                return true;
            }
            return false;
        }
        boolean any = false;
        if (focusR && Character.isDigit(chr)) {
            inputR += chr;
            inputR = clampNumeric(inputR);
            any = true;
        }
        if (focusG && Character.isDigit(chr)) {
            inputG += chr;
            inputG = clampNumeric(inputG);
            any = true;
        }
        if (focusB && Character.isDigit(chr)) {
            inputB += chr;
            inputB = clampNumeric(inputB);
            any = true;
        }
        if (focusA && Character.isDigit(chr)) {
            inputA += chr;
            inputA = clampNumeric(inputA);
            any = true;
        }
        if (any) {
            applyInputsIfValid();
            return true;
        }
        return false;
    }

    private void setFocus(boolean hex, boolean r, boolean g, boolean b, boolean a) {
        this.focusHex = hex;
        this.focusR = r;
        this.focusG = g;
        this.focusB = b;
        this.focusA = a;
    }

    private void updateRFromMouse(double mouseX) {
        double t = (mouseX - rSliderX) / Math.max(1, rSliderW);
        int v = (int) Math.round(Math.max(0, Math.min(1, t)) * 255);
        colorSetting.setValue(v, colorSetting.getGreen(), colorSetting.getBlue(), colorSetting.isHasAlpha() ? colorSetting.getAlpha() : 255);
    }

    private void updateGFromMouse(double mouseX) {
        double t = (mouseX - gSliderX) / Math.max(1, gSliderW);
        int v = (int) Math.round(Math.max(0, Math.min(1, t)) * 255);
        colorSetting.setValue(colorSetting.getRed(), v, colorSetting.getBlue(), colorSetting.isHasAlpha() ? colorSetting.getAlpha() : 255);
    }

    private void updateBFromMouse(double mouseX) {
        double t = (mouseX - bSliderX) / Math.max(1, bSliderW);
        int v = (int) Math.round(Math.max(0, Math.min(1, t)) * 255);
        colorSetting.setValue(colorSetting.getRed(), colorSetting.getGreen(), v, colorSetting.isHasAlpha() ? colorSetting.getAlpha() : 255);
    }

    private void updateAFromMouse(double mouseX) {
        double t = (mouseX - aSliderX) / Math.max(1, aSliderW);
        int v = (int) Math.round(Math.max(0, Math.min(1, t)) * 255);
        colorSetting.setValue(colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue(), v);
    }


    public int getWidth() {
        return SIZE + SPACING + BAR_WIDTH + (colorSetting.isHasAlpha() ? SPACING + BAR_WIDTH : 0);
    }

    public int getHeight() {
        return SIZE;
    }

    public int getTotalHeight() {
        int rows = 1 + 3 + (colorSetting != null && colorSetting.isHasAlpha() ? 1 : 0);
        return SIZE + 10 + rows * (CONTROL_HEIGHT + CONTROL_SPACING);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public void toggleExpanded() {
        this.expanded = !this.expanded;
    }

    public void renderCompact(DrawContext context, int x, int y, int width, int height) {
        this.posX = x;
        this.posY = y;

        int size = Math.min(COMPACT_SIZE, Math.min(width - 40, height - 20));
        int barWidth = COMPACT_BAR_WIDTH;
        int spacing = COMPACT_SPACING;

        renderColorSquareCompact(context, x, y, size);

        int brightnessX = x + size + spacing;
        renderGradientBarCompact(context, brightnessX, y, size, barWidth, true);

        if (colorSetting != null && colorSetting.isHasAlpha()) {
            int alphaX = brightnessX + barWidth + spacing;
            renderGradientBarCompact(context, alphaX, y, size, barWidth, false);
        }

        renderSelectionIndicatorsCompact(context, x, y, size, barWidth, spacing);
    }

    private void renderColorSquareCompact(DrawContext context, int x, int y, int size) {
        if (colorSetting == null) return;

        setupRendering(context);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float brightness = colorSetting.getHSB()[2];
        int steps = 25;

        for (int i = 0; i < steps; i++) {
            for (int j = 0; j < steps; j++) {
                float[] hues = {(float) i / steps, (float) (i + 1) / steps};
                float[] sats = {(float) j / steps, (float) (j + 1) / steps};

                Color[] colors = {
                        Color.getHSBColor(hues[0], sats[0], brightness),
                        Color.getHSBColor(hues[1], sats[0], brightness),
                        Color.getHSBColor(hues[1], sats[1], brightness),
                        Color.getHSBColor(hues[0], sats[1], brightness)
                };

                float[] xs = {x + i * size / steps, x + (i + 1) * size / steps};
                float[] ys = {y + j * size / steps, y + (j + 1) * size / steps};

                addQuad(buffer, matrix, xs, ys, colors);
            }
        }

        finishRendering(buffer, context, x, y, size, size);
    }

    private void renderGradientBarCompact(DrawContext context, int x, int y, int height, int barWidth, boolean isBrightness) {
        if (colorSetting == null) return;

        if (!isBrightness) renderCheckerboard(context, x, y, barWidth, height);

        setupRendering(context);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float[] hsb = colorSetting.getHSB();
        Color baseColor = colorSetting.getValue();

        for (int i = 0; i < 15; i++) {
            float t1 = (float) i / 15, t2 = (float) (i + 1) / 15;
            float y1 = y + t1 * height, y2 = y + t2 * height;

            Color color1, color2;
            if (isBrightness) {
                color1 = Color.getHSBColor(hsb[0], hsb[1], 1 - t1);
                color2 = Color.getHSBColor(hsb[0], hsb[1], 1 - t2);
            } else {
                int alpha1 = (int) ((1 - t1) * 255), alpha2 = (int) ((1 - t2) * 255);
                color1 = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha1);
                color2 = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha2);
            }

            addRect(buffer, matrix, x, y1, x + barWidth, y2, color1, color2);
        }

        finishRendering(buffer, context, x, y, barWidth, height);
    }

    private void renderSelectionIndicatorsCompact(DrawContext context, int x, int y, int size, int barWidth, int spacing) {
        if (colorSetting == null) return;

        float[] hsb = colorSetting.getHSB();

        int squareX = (int) (x + hsb[0] * size), squareY = (int) (y + hsb[1] * size);
        drawCrosshair(context, squareX, squareY);

        int brightnessX = x + size + spacing;
        int brightnessY = (int) (y + (1 - hsb[2]) * size);
        drawBarIndicator(context, brightnessX, brightnessY, barWidth);

        if (colorSetting.isHasAlpha()) {
            int alphaX = brightnessX + barWidth + spacing;
            int alphaY = (int) (y + (1 - colorSetting.getAlpha() / 255f) * size);
            drawBarIndicator(context, alphaX, alphaY, barWidth);
        }
    }
}