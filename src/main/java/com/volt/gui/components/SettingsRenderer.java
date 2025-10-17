package com.volt.gui.components;

import com.volt.gui.events.GuiEventHandler;
import com.volt.module.Module;
import com.volt.module.setting.*;
import com.volt.utils.render.font.fonts.FontRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SettingsRenderer {
    private static final int SETTING_HEIGHT = 28;
    private static final Map<Setting, Float> animatedValues = new HashMap<>();

    public static int renderModuleSettings(DrawContext context, Module module, int x, int moduleY, int width, float animation, FontRenderer smallFont, Map<ModeSetting, Boolean> dropdownExpanded, GuiEventHandler eventHandler) {
        if (module.getSettings() == null || module.getSettings().isEmpty()) {
            return 0;
        }

        MatrixStack matrices = context.getMatrices();

        int settingsHeight = module.getSettings().size() * SETTING_HEIGHT;
        float effectiveAnimation = animation < 0.22f ? 0f : animation;
        int animatedHeight = (int) (settingsHeight * effectiveAnimation);

        if (animatedHeight <= 0) return settingsHeight;

        int baseR = (int) MathHelperLerp(15, 28, effectiveAnimation);
        int baseG = (int) MathHelperLerp(15, 28, effectiveAnimation);
        int baseB = (int) MathHelperLerp(25, 34, effectiveAnimation);
        context.fill(x + 18, moduleY, x + width - 18, moduleY + animatedHeight, new Color(baseR, baseG, baseB, 200).getRGB());

        int maxControlWidth = Math.min(100, width - 78);

        int settingY = moduleY + 5;
        for (Setting setting : module.getSettings()) {
            String settingName = setting.getName();
            float maxNameWidth = width - 58 - maxControlWidth - 20;
            while (smallFont.getStringWidth(settingName + "...") > maxNameWidth && settingName.length() > 1) {
                settingName = settingName.substring(0, settingName.length() - 1);
            }
            if (smallFont.getStringWidth(settingName) > maxNameWidth) settingName += "...";

            smallFont.drawString(matrices, settingName, x + 28, settingY + 5, new Color(200, 200, 200));

            renderSettingControl(context, setting, x, settingY, width, maxControlWidth, null, eventHandler, smallFont);

            settingY += SETTING_HEIGHT;
            if (settingY > moduleY + animatedHeight) break;
        }

        for (int i = 0; i < module.getSettings().size(); i++) {
            Setting setting = module.getSettings().get(i);
            if (setting instanceof ModeSetting modeSetting && dropdownExpanded.getOrDefault(modeSetting, false)) {
                int controlX = x + width - 18 - maxControlWidth - 12;
                int controlY = moduleY + 5 + i * SETTING_HEIGHT + (SETTING_HEIGHT - 15) / 2;
                int controlWidth = maxControlWidth;
                int controlHeight = 15;

                renderModeDropdownOverlay(context, modeSetting, controlX, controlY, controlWidth, controlHeight);
            }
        }

        return settingsHeight;
    }

    private static void renderModeDropdownOverlay(DrawContext context, ModeSetting modeSetting, int x, int y, int width, int height) {
        String currentMode = modeSetting.getMode();

        context.fill(x, y, x + width, y + height, new Color(70, 70, 90).getRGB());
        context.drawBorder(x, y, width, height, new Color(100, 100, 100).getRGB());
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, currentMode, x + 4, y + 3, new Color(255, 255, 255).getRGB());

        int arrowX = x + width - 12;
        int arrowY = y + height / 2;
        context.fill(arrowX, arrowY - 2, arrowX + 5, arrowY - 1, new Color(200, 200, 200).getRGB());
        context.fill(arrowX + 1, arrowY - 1, arrowX + 4, arrowY, new Color(200, 200, 200).getRGB());
        context.fill(arrowX + 2, arrowY, arrowX + 3, arrowY + 1, new Color(200, 200, 200).getRGB());

        int optionY = y + height;
        for (String mode : modeSetting.getModes()) {
            boolean isSelected = mode.equals(currentMode);
            Color bg = isSelected ? new Color(100, 150, 255) : new Color(70, 70, 90);

            context.fill(x, optionY, x + width, optionY + height, bg.getRGB());
            context.drawBorder(x, optionY, width, height, new Color(80, 80, 80).getRGB());

            String text = mode.length() > 8 ? mode.substring(0, 8) + "..." : mode;
            Color textColor = isSelected ? new Color(150, 100, 255) : new Color(200, 200, 200);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x + 4, optionY + 3, textColor.getRGB());

            optionY += height;
        }
    }

    public static int renderModuleSettingsInGrid(DrawContext context, Module module, int x, int moduleY, int width, float animation, FontRenderer smallFont) {
        if (module.getSettings() == null || module.getSettings().isEmpty()) {
            return 0;
        }

        MatrixStack matrices = context.getMatrices();
        int rows = (int) Math.ceil(module.getSettings().size() / 2.0);
        int settingsHeight = rows * SETTING_HEIGHT;
        int animatedHeight = (int) (settingsHeight * animation);

        context.fill(x + 18, moduleY, x + width - 18, moduleY + animatedHeight,
                new Color(15, 15, 25, 200).getRGB());

        int columnWidth = (width - 58) / 2;
        int maxNameWidth = columnWidth - 120;

        for (int i = 0; i < module.getSettings().size(); i++) {
            var setting = module.getSettings().get(i);

            int row = i / 2;
            int col = i % 2;
            int settingX = x + 28 + col * columnWidth;
            int settingY = moduleY + 5 + row * SETTING_HEIGHT;

            String settingName = setting.getName();
            float nameWidth = smallFont.getStringWidth(settingName);
            if (nameWidth > maxNameWidth) {
                while (smallFont.getStringWidth(settingName + "...") > maxNameWidth && settingName.length() > 1) {
                    settingName = settingName.substring(0, settingName.length() - 1);
                }
                settingName += "...";
            }
            smallFont.drawString(matrices, settingName,
                    settingX, settingY + 5, new Color(200, 200, 200));

            renderSettingControlInGrid(context, setting, settingX, settingY, columnWidth, smallFont);
        }

        return settingsHeight;
    }

    private static void renderSettingControl(DrawContext context, Setting setting, int x, int settingY, int width, int maxControlWidth, Map<ModeSetting, Boolean> dropdownExpanded, GuiEventHandler eventHandler, FontRenderer smallFont) {
        int controlX = x + width - 18 - maxControlWidth - 12;
        int controlY = settingY + (SETTING_HEIGHT - 15) / 2;
        int controlWidth = maxControlWidth;
        int controlHeight = 15;

        renderSettingControlCommon(context, setting, controlX, controlY, controlWidth, controlHeight, dropdownExpanded, eventHandler, smallFont);
    }

    private static void renderSettingControlInGrid(DrawContext context, Setting setting, int settingX, int settingY, int columnWidth, FontRenderer smallFont) {
        int maxControlWidth = Math.min(80, columnWidth - 20);
        int controlX = settingX + columnWidth - maxControlWidth - 10;
        int controlY = settingY + (SETTING_HEIGHT - 15) / 2;
        int controlWidth = maxControlWidth;
        int controlHeight = 15;

        renderSettingControlCommon(context, setting, controlX, controlY, controlWidth, controlHeight, null, null, smallFont);
    }

    private static void renderSettingControlCommon(DrawContext context, Setting setting, int controlX, int controlY, int controlWidth, int controlHeight, Map<ModeSetting, Boolean> dropdownExpanded, GuiEventHandler eventHandler, FontRenderer smallFont) {
        float prev = animatedValues.getOrDefault(setting, 0f);
        switch (setting) {
            case BooleanSetting booleanSetting -> {
                boolean enabled = booleanSetting.getValue();
                float target = enabled ? 1f : 0f;
                float cur = prev + (target - prev) * 0.14f;
                animatedValues.put(setting, cur);

                Color borderColor = mixColor(new Color(100, 100, 100), new Color(124, 77, 255), cur);
                Color fillColor = mixColor(new Color(40, 40, 40), new Color(124, 77, 255, 200), cur);

                int checkboxSize = 12;
                UIRenderer.renderCheckbox(context, controlX, controlY + 2, checkboxSize, enabled, borderColor, fillColor);
            }

            case NumberSetting numberSetting -> {
                double min = numberSetting.getMin();
                double max = numberSetting.getMax();
                double value = numberSetting.getValue();
                double normalized = (value - min) / (max - min);
                float cur = prev + ((float) normalized - prev) * 0.12f;
                animatedValues.put(setting, cur);

                int sliderHeight = 8;
                int sliderY = controlY + (controlHeight - sliderHeight) / 2;
                controlWidth = (int) (((double) controlWidth) * .75);
                UIRenderer.renderSlider(context, controlX, sliderY, controlWidth, sliderHeight, cur,
                        new Color(48, 48, 48), new Color(124, 77, 255));

                boolean isEditing = eventHandler != null && eventHandler.getEditingNumberSetting() == numberSetting;

                if (isEditing) {
                    String inputText = eventHandler.getNumberInputText();
                    int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(inputText + "|");
                    int inputX = controlX + controlWidth + 5;
                    int inputY = controlY + 1;
                    int inputWidth = Math.max(30, textWidth + 4);
                    int inputHeight = controlHeight - 2;

                    context.fill(inputX - 2, inputY, inputX + inputWidth, inputY + inputHeight,
                            new Color(28, 28, 32, 220).getRGB());
                    context.drawBorder(inputX - 2, inputY, inputWidth, inputHeight,
                            new Color(124, 77, 255).getRGB());

                    String displayText = inputText + "|";
                    smallFont.drawString(context.getMatrices(), displayText, inputX, controlY + 2, new Color(255, 255, 255));
                } else {
                    String valueText = String.valueOf(value);
                    if (valueText.endsWith(".0")) {
                        valueText = valueText.substring(0, valueText.length() - 2);
                    }
                    context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, valueText,
                            controlX + controlWidth + 5, controlY + 2, new Color(170, 170, 170).getRGB());
                }
            }

            case ModeSetting modeSetting -> {
                String currentMode = modeSetting.getMode();
                String displayText = currentMode.length() > 8 ? currentMode.substring(0, 8) + "..." : currentMode;

                context.fill(controlX, controlY, controlX + controlWidth, controlY + controlHeight,
                        new Color(64, 64, 72).getRGB());
                context.drawBorder(controlX, controlY, controlWidth, controlHeight,
                        new Color(96, 96, 96).getRGB());

                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayText,
                        controlX + 4, controlY + 3, new Color(255, 255, 255).getRGB());

                int arrowX = controlX + controlWidth - 12;
                int arrowY = controlY + controlHeight / 2;
                boolean isExpanded = dropdownExpanded != null && dropdownExpanded.getOrDefault(modeSetting, false);

                if (isExpanded) {
                    context.fill(arrowX + 2, arrowY - 2, arrowX + 3, arrowY + 1, new Color(200, 200, 200).getRGB());
                    context.fill(arrowX + 1, arrowY - 1, arrowX + 4, arrowY, new Color(200, 200, 200).getRGB());
                    context.fill(arrowX, arrowY, arrowX + 5, arrowY + 1, new Color(200, 200, 200).getRGB());
                } else {
                    context.fill(arrowX, arrowY - 2, arrowX + 5, arrowY - 1, new Color(200, 200, 200).getRGB());
                    context.fill(arrowX + 1, arrowY - 1, arrowX + 4, arrowY, new Color(200, 200, 200).getRGB());
                    context.fill(arrowX + 2, arrowY, arrowX + 3, arrowY + 1, new Color(200, 200, 200).getRGB());
                }

                if (isExpanded) {
                    int optionY = controlY + controlHeight;
                    for (String mode : modeSetting.getModes()) {
                        boolean isSelected = mode.equals(currentMode);
                        Color optionBg = isSelected ? new Color(100, 150, 255) : new Color(70, 70, 90);

                        context.fill(controlX, optionY, controlX + controlWidth, optionY + controlHeight,
                                optionBg.getRGB());
                        context.drawBorder(controlX, optionY, controlWidth, controlHeight,
                                new Color(80, 80, 80).getRGB());

                        String optionText = mode.length() > 8 ? mode.substring(0, 8) + "..." : mode;
                        Color textColor = isSelected ? new Color(150, 100, 255) : new Color(200, 200, 200);
                        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, optionText,
                                controlX + 4, optionY + 3, textColor.getRGB());

                        optionY += controlHeight;
                    }
                }
            }

            case KeybindSetting keybindSetting -> {
                String displayKey = keybindSetting.isListening() ? "..." : com.volt.utils.keybinding.KeyUtils.getKey(keybindSetting.getKeyCode());

                context.fill(controlX, controlY, controlX + controlWidth, controlY + controlHeight, new Color(36, 36, 40).getRGB());
                context.drawBorder(controlX, controlY, controlWidth, controlHeight, new Color(90, 90, 90).getRGB());
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayKey, controlX + 4, controlY + 3, new Color(255, 255, 255).getRGB());
            }

            case ColorSetting colorSetting -> {
                Color currentColor = colorSetting.getValue();

                int previewSize = Math.min(controlHeight - 2, 12);
                int previewX = controlX + controlWidth - previewSize - 2;
                int previewY = controlY + (controlHeight - previewSize) / 2;

                if (colorSetting.isHasAlpha()) {
                    renderCheckerboard(context, previewX, previewY, previewSize, previewSize);
                }

                context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, currentColor.getRGB());
                context.drawBorder(previewX, previewY, previewSize, previewSize, new Color(100, 100, 100).getRGB());

                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Color (Click)",
                        controlX + 4, controlY + 3, new Color(200, 200, 200).getRGB());
            }

            default -> {
            }
        }
    }

    private static void renderCheckerboard(DrawContext context, int x, int y, int width, int height) {
        int checkSize = 4;
        for (int i = 0; i < width; i += checkSize) {
            for (int j = 0; j < height; j += checkSize) {
                boolean isLight = ((i / checkSize) + (j / checkSize)) % 2 == 0;
                Color checkColor = isLight ? new Color(200, 200, 200) : new Color(150, 150, 150);

                int checkWidth = Math.min(checkSize, width - i);
                int checkHeight = Math.min(checkSize, height - j);

                context.fill(x + i, y + j, x + i + checkWidth, y + j + checkHeight, checkColor.getRGB());
            }
        }
    }

    public static int getModuleSettingsHeight(Module module, int settingHeight, GuiEventHandler eventHandler) {
        if (module.getSettings() == null || module.getSettings().isEmpty()) {
            return 0;
        }

        int totalHeight = module.getSettings().size() * settingHeight;

        if (eventHandler != null) {
            for (Setting setting : module.getSettings()) {
                if (setting instanceof ColorSetting colorSetting && eventHandler.getColorPickerExpanded().getOrDefault(colorSetting, false)) {
                    totalHeight += 130;
                }
            }
        }

        return totalHeight;
    }

    private static float MathHelperLerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static Color mixColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = (int) MathHelperLerp(a.getRed(), b.getRed(), t);
        int g = (int) MathHelperLerp(a.getGreen(), b.getGreen(), t);
        int bl = (int) MathHelperLerp(a.getBlue(), b.getBlue(), t);
        int alpha = (int) MathHelperLerp(a.getAlpha(), b.getAlpha(), t);
        return new Color(Math.max(0, Math.min(255, r)), Math.max(0, Math.min(255, g)), Math.max(0, Math.min(255, bl)), Math.max(0, Math.min(255, alpha)));
    }
}