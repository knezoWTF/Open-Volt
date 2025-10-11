package com.volt.gui;

import com.volt.gui.components.ColorPicker;
import com.volt.gui.events.GuiEventHandler;
import com.volt.module.setting.ColorSetting;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;
import java.util.Map;

public class ColorPickerManager {
    private final GuiEventHandler eventHandler;

    public ColorPickerManager(GuiEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void renderColorPickerPanel(DrawContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (!eventHandler.isAnyColorPickerExpanded()) return;

        context.fill(x, y, x + width, y + height, new Color(25, 25, 35, 240).getRGB());
        context.drawBorder(x, y, width, height, new Color(100, 100, 100).getRGB());

        context.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, "Color Pickers", x + 10, y + 10, new Color(255, 255, 255).getRGB());

        int currentY = y + 35;
        int panelPadding = 10;

        for (Map.Entry<ColorSetting, ColorPicker> entry : eventHandler.getColorPickers().entrySet()) {
            ColorSetting colorSetting = entry.getKey();
            ColorPicker colorPicker = entry.getValue();

            if (eventHandler.getColorPickerExpanded().getOrDefault(colorSetting, false)) {
                context.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, colorSetting.getName(),
                        x + panelPadding, currentY, new Color(200, 200, 200).getRGB());
                currentY += 20;

                int pickerHeight = Math.max(200, colorPicker.getTotalHeight());

                if (currentY + pickerHeight > y + height - panelPadding) {
                    pickerHeight = y + height - panelPadding - currentY;
                }

                if (pickerHeight > 50) {
                    int clipLeft = x + panelPadding;
                    int clipTop = currentY;
                    int clipRight = x + width - panelPadding;
                    int clipBottom = y + height - panelPadding;
                    context.enableScissor(clipLeft, clipTop, clipRight, clipBottom);
                    colorPicker.render(context, x + panelPadding, currentY, mouseX, mouseY);
                    context.disableScissor();
                }

                currentY += pickerHeight + 15;

                if (currentY < y + height - 50) {
                    context.fill(x + panelPadding, currentY, x + width - panelPadding, currentY + 1,
                            new Color(100, 100, 100).getRGB());
                    currentY += 10;
                }
            }
        }
    }

    public boolean handleColorPickerClicks(double mouseX, double mouseY, int button) {
        if (!eventHandler.isAnyColorPickerExpanded()) return false;

        for (Map.Entry<ColorSetting, ColorPicker> entry : eventHandler.getColorPickers().entrySet()) {
            ColorSetting colorSetting = entry.getKey();
            ColorPicker colorPicker = entry.getValue();

            if (eventHandler.getColorPickerExpanded().getOrDefault(colorSetting, false)) {
                if (colorPicker.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean handleColorPickerDrag(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!eventHandler.isAnyColorPickerExpanded()) return false;

        for (Map.Entry<ColorSetting, ColorPicker> entry : eventHandler.getColorPickers().entrySet()) {
            ColorSetting colorSetting = entry.getKey();
            ColorPicker colorPicker = entry.getValue();

            if (eventHandler.getColorPickerExpanded().getOrDefault(colorSetting, false)) {
                if (colorPicker.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean handleColorPickerRelease(double mouseX, double mouseY, int button) {
        if (!eventHandler.isAnyColorPickerExpanded()) return false;

        for (Map.Entry<ColorSetting, ColorPicker> entry : eventHandler.getColorPickers().entrySet()) {
            ColorSetting colorSetting = entry.getKey();
            ColorPicker colorPicker = entry.getValue();

            if (eventHandler.getColorPickerExpanded().getOrDefault(colorSetting, false)) {
                if (colorPicker.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean handleColorPickerKeyPress(int keyCode) {
        if (!eventHandler.isAnyColorPickerExpanded()) return false;
        boolean handled = false;
        for (Map.Entry<ColorSetting, ColorPicker> entry : eventHandler.getColorPickers().entrySet()) {
            ColorSetting colorSetting = entry.getKey();
            ColorPicker colorPicker = entry.getValue();
            if (eventHandler.getColorPickerExpanded().getOrDefault(colorSetting, false)) {
                try {
                    java.lang.reflect.Method m = ColorPicker.class.getDeclaredMethod("keyPressed", int.class);
                    m.setAccessible(true);
                    handled = (boolean) m.invoke(colorPicker, keyCode) || handled;
                } catch (Throwable ignored) {
                }
            }
        }
        return handled;
    }

    public boolean handleColorPickerCharTyped(char chr) {
        if (!eventHandler.isAnyColorPickerExpanded()) return false;
        boolean handled = false;
        for (Map.Entry<ColorSetting, ColorPicker> entry : eventHandler.getColorPickers().entrySet()) {
            ColorSetting colorSetting = entry.getKey();
            ColorPicker colorPicker = entry.getValue();
            if (eventHandler.getColorPickerExpanded().getOrDefault(colorSetting, false)) {
                try {
                    java.lang.reflect.Method m = ColorPicker.class.getDeclaredMethod("charTyped", char.class);
                    m.setAccessible(true);
                    handled = (boolean) m.invoke(colorPicker, chr) || handled;
                } catch (Throwable ignored) {
                }
            }
        }
        return handled;
    }
}