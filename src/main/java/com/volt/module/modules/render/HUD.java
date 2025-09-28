package com.volt.module.modules.render;

import com.volt.Volt;
import com.volt.event.impl.render.EventRender2D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.ModeSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.module.setting.StringSetting;
import com.volt.utils.font.FontManager;
import com.volt.utils.font.fonts.FontRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HUD extends Module {
    public static final BooleanSetting watermark = new BooleanSetting("Watermark", true);
    public static final ModeSetting watermarkMode = new ModeSetting("Watermark Mode", "Simple", "Simple", "Gamesense");
    public static final StringSetting watermarkText = new StringSetting("Watermark Text", "Volt");
    public static final ModeSetting watermarkSimpleFontMode = new ModeSetting("Watermark Font", "MC", "MC", "Inter", "JetbrainsMono", "Poppins");
    public static final NumberSetting watermarkScale = new NumberSetting("Watermark Scale", 0.5, 3.0, 1.0, 0.1);
    public static final BooleanSetting arrayList = new BooleanSetting("ArrayList", true);
    public static final NumberSetting arrayListScale = new NumberSetting("ArrayList Scale", 0.5, 3.0, 1.0, 0.1);
    public static final ModeSetting colorMode = new ModeSetting("Color Mode", "Astolfo", "Astolfo", "Theme", "Custom");
    public static final ColorSetting customColor = new ColorSetting("Custom Color", new Color(255, 255, 255));
    public static final ModeSetting fontMode = new ModeSetting("ArrayList Font", "MC", "MC", "Inter", "JetbrainsMono", "Poppins");
    public static final ModeSetting suffixMode = new ModeSetting("Suffix", "Space", "Space", "-", ">", "[ ]");
    public static final BooleanSetting hideVisuals = new BooleanSetting("Hide visuals", true);
    public static final BooleanSetting lowercase = new BooleanSetting("Lowercase", false);
    public static final ModeSetting backBarMode = new ModeSetting("Backbar Mode", "None", "None", "Full", "Rise");
    public static final BooleanSetting waveAnimation = new BooleanSetting("Wave Animation", false);
    public static final NumberSetting waveSpeed = new NumberSetting("Wave Speed", 0.1, 5.0, 1.5, 0.1);
    public static final NumberSetting waveSpread = new NumberSetting("Wave Spread", 0.1, 5.0, 0.6, 0.1);
    public static final NumberSetting padding = new NumberSetting("Offset", 0, 40, 5, 1);
    public static final NumberSetting opacity = new NumberSetting("BG Opacity", 0, 255, 80, 1);
    public static final BooleanSetting info = new BooleanSetting("Info", true);
    public static final BooleanSetting bpsCounter = new BooleanSetting("BPS", true);
    public static final BooleanSetting fpsCounter = new BooleanSetting("FPS", true);
    public static final NumberSetting scale = new NumberSetting("Scale", 0.5, 3.0, 1.0, 0.1);

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public HUD() {
        super("HUD", "Renders information", -1, Category.RENDER);
        addSettings(watermark, watermarkMode, watermarkText, watermarkSimpleFontMode, watermarkScale, arrayList, arrayListScale, colorMode, customColor, fontMode, suffixMode, hideVisuals, lowercase, backBarMode, waveAnimation, waveSpeed, waveSpread, padding, opacity, info, bpsCounter, fpsCounter, scale);
    }

    @EventHandler
    private void onEventRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (mc.currentScreen != null) {
            return;
        }

        if (watermark.getValue()) {
            switch (watermarkMode.getMode()) {
                case "Simple":
                    if (!watermarkText.getValue().isEmpty()) {
                        String watermark = watermarkText.getValue();

                        char firstCharacter = watermark.charAt(0);
                        String restOfString = watermark.substring(1);

                        String totalWatermarkText = firstCharacter + Formatting.WHITE.toString() + restOfString;

                         int scaledX = (int) (3 * watermarkScale.getValue());
                         int scaledY = (int) (3 * watermarkScale.getValue());
                         
                         Color watermarkColor = switch (colorMode.getMode()) {
                             case "Astolfo" -> new Color(getAstolfo(0));
                             case "Custom" -> getCustomColor();
                             default -> getThemeColor();
                         };

                        if (watermarkSimpleFontMode.getMode().equals("MC")) {
                            event.getContext().getMatrices().push();
                            event.getContext().getMatrices().scale((float) watermarkScale.getValue(), (float) watermarkScale.getValue(), 1.0f);
                            
                            int adjustedX = (int) (scaledX / watermarkScale.getValue());
                            int adjustedY = (int) (scaledY / watermarkScale.getValue());
                            
                            event.getContext().drawText(mc.textRenderer, totalWatermarkText, adjustedX, adjustedY, watermarkColor.getRGB(), true);
                            event.getContext().getMatrices().pop();
                        } else {
                            getWatermarkFontRenderer(watermarkSimpleFontMode.getMode()).drawString(event.getContext().getMatrices(), totalWatermarkText, scaledX, scaledY, watermarkColor);
                        }
                    }
                    break;

                case "Gamesense":
                     String text = "§f" + watermarkText.getValue() + "§rsense §8| §f " + ("free") + "§7 (" + ("0000") + ") §8 | §f " + getIP();

                     int padding = (int) (2 * watermarkScale.getValue());
                     int offsetX = (int) (4 * watermarkScale.getValue());
                     int offsetY = (int) (4 * watermarkScale.getValue());

                     FontRenderer gamesenseFont = getWatermarkFontRenderer("Inter");
                     int textWidth = (int) gamesenseFont.getStringWidth(text);
                     int textHeight = (int) gamesenseFont.getStringHeight(text);
                     int backgroundWidth = textWidth + padding * 2;
                     int backgroundHeight = textHeight + padding * 2;

                     event.getContext().fill(
                             offsetX - padding - 1,
                             offsetY - padding - 1,
                             offsetX + backgroundWidth + 1,
                             offsetY + backgroundHeight + 1,
                             new Color(96, 96, 96).getRGB()
                     );

                     event.getContext().fill(
                             offsetX - padding,
                             offsetY - padding,
                             offsetX + backgroundWidth,
                             offsetY + backgroundHeight,
                             new Color(25, 25, 25).getRGB()
                     );

                     Color gamesenseColor = switch (colorMode.getMode()) {
                         case "Astolfo" -> new Color(getAstolfo(0));
                         case "Custom" -> getCustomColor();
                         default -> getThemeColor();
                     };
                     
                     gamesenseFont.drawString(
                             event.getContext().getMatrices(),
                             text,
                             offsetX,
                             offsetY,
                             gamesenseColor
                     );
                     break;
            }
        }

        if (arrayList.getValue()) {
             List<Module> enabledModules = new ArrayList<>();
             for (Module module : Volt.INSTANCE.getModuleManager().getModules()) {
                 if (!module.isEnabled()) continue;
                 if (hideVisuals.getValue() && module.getModuleCategory() == Category.RENDER) continue;
                 enabledModules.add(module);
             }

            if (fontMode.getMode().equals("MC")) {
                enabledModules.sort(Comparator.comparingDouble(ri -> -mc.textRenderer.getWidth(getFullName(ri))));
            } else {
                enabledModules.sort(Comparator.comparingDouble(ri -> -getCustomFontRenderer(fontMode.getMode()).getStringWidth(getFullName(ri))));
            }

             int i = padding.getValueInt();
            int totalWidth = event.getWidth();

             for (Module m : enabledModules) {
                 int moduleHeight;

                 if (fontMode.getMode().equals("MC")) {
                     moduleHeight = mc.textRenderer.fontHeight;
                 } else {
                     moduleHeight = (int) getCustomFontRenderer(fontMode.getMode()).getStringHeight(getFullName(m));
                 }

                 i += moduleHeight + (int) (3 * arrayListScale.getValue());
             }

            i = padding.getValueInt();
            double waveTime = (System.currentTimeMillis() / 1000.0) * waveSpeed.getValue();
            int moduleIndex = 0;

            for (Module m : enabledModules) {
                String fullName = getFullName(m);
                int color = switch (colorMode.getMode()) {
                    case "Astolfo" -> getAstolfo(i * 3);
                    case "Theme" -> getThemeColor().getRGB();
                    case "Custom" -> getCustomColor().getRGB();
                    default -> 0;
                };

                int textColor = color;
                if (waveAnimation.getValue()) {
                    double wavePhase = waveTime + moduleIndex * waveSpread.getValue();
                    double waveValue = (Math.sin(wavePhase) + 1.0) * 0.5;
                    textColor = applyWaveColor(color, waveValue);
                }

                int moduleWidth;
                int moduleHeight;
                FontRenderer customRenderer = null;

                if (fontMode.getMode().equals("MC")) {
                    moduleWidth = mc.textRenderer.getWidth(fullName);
                    moduleHeight = mc.textRenderer.fontHeight;
                } else {
                    customRenderer = getCustomFontRenderer(fontMode.getMode());
                    moduleWidth = (int) customRenderer.getStringWidth(fullName);
                    moduleHeight = (int) customRenderer.getStringHeight(fullName);
                }

                int scaledPadding1 = (int) (1 * arrayListScale.getValue());
                int scaledPadding2 = (int) (2 * arrayListScale.getValue());
                int scaledPadding3 = (int) (3 * arrayListScale.getValue());
                int scaledPadding5 = (int) (5 * arrayListScale.getValue());

                if (opacity.getValueInt() > 0) {
                    int backgroundColor = new Color(0, 0, 0, opacity.getValueInt()).getRGB();
                    int backgroundLeft = totalWidth - moduleWidth - padding.getValueInt() - scaledPadding3;
                    int backgroundTop = i - scaledPadding2;
                    int backgroundRight = totalWidth - padding.getValueInt() + scaledPadding5;
                    int backgroundBottom = i + moduleHeight + scaledPadding1;
                    event.getContext().fill(backgroundLeft, backgroundTop, backgroundRight, backgroundBottom, backgroundColor);
                }

                switch (backBarMode.getMode()) {
                    case "Full" -> event.getContext().fill(totalWidth - padding.getValueInt() + scaledPadding3, i - scaledPadding2, totalWidth - padding.getValueInt() + scaledPadding5, i + moduleHeight + scaledPadding1, color);
                    case "Rise" -> event.getContext().fill(totalWidth - padding.getValueInt() + scaledPadding3, i - scaledPadding1, totalWidth - padding.getValueInt() + scaledPadding5, i + moduleHeight, color);
                }

                if (fontMode.getMode().equals("MC")) {
                    event.getContext().drawText(mc.textRenderer, fullName, totalWidth - moduleWidth - padding.getValueInt(), i, textColor, true);
                } else {
                    customRenderer.drawString(event.getContext().getMatrices(), fullName, totalWidth - moduleWidth - padding.getValueInt(), i, new Color(textColor));
                }

                i += moduleHeight + (int) (3 * arrayListScale.getValue());
                moduleIndex++;
            }
        }

        if (info.getValue()) {
            String bps = String.valueOf(decimalFormat.format(Math.hypot(mc.player.getX() - mc.player.prevX, mc.player.getZ() - mc.player.prevZ) * 20.0F));
            String fps = String.valueOf(MinecraftClient.getInstance().getCurrentFps());

            int x = (int) (10 * scale.getValue());

            int lineHeight = (int) (mc.textRenderer.fontHeight * scale.getValue());

            int y = event.getHeight() - (fpsCounter.getValue() && bpsCounter.getValue() ? 2 * lineHeight : lineHeight) - (int) (10 * scale.getValue());

            if (fpsCounter.getValue()) {
                int color = switch (colorMode.getMode()) {
                    case "Astolfo" -> getAstolfo(3);
                    case "Theme" -> getThemeColor(1).getRGB();
                    case "Custom" -> getCustomColor().getRGB();
                    default -> 0;
                };
                event.getContext().drawText(mc.textRenderer, "FPS: " + fps, x, y, color, true);
                y -= lineHeight;
            }

            if (bpsCounter.getValue()) {
                int color = switch (colorMode.getMode()) {
                    case "Astolfo" -> getAstolfo(6);
                    case "Theme" -> getThemeColor(2).getRGB();
                    case "Custom" -> getCustomColor().getRGB();
                    default -> 0;
                };
                event.getContext().drawText(mc.textRenderer, "BPS: " + bps, x, y, color, true);
            }
        }
    }

    private FontRenderer getCustomFontRenderer(String name) {
        int scaledSize = (int) (16 * arrayListScale.getValue());
        return switch (name) {
            case "JetbrainsMono" -> Volt.INSTANCE.fontManager.getSize(scaledSize, FontManager.Type.JetbrainsMono);
            case "Poppins" -> Volt.INSTANCE.fontManager.getSize(scaledSize, FontManager.Type.Poppins);
            default -> Volt.INSTANCE.fontManager.getSize(scaledSize, FontManager.Type.Inter);
        };
    }

    private FontRenderer getWatermarkFontRenderer(String name) {
        int scaledSize = (int) (16 * watermarkScale.getValue());
        return switch (name) {
            case "JetbrainsMono" -> Volt.INSTANCE.fontManager.getSize(scaledSize, FontManager.Type.JetbrainsMono);
            case "Poppins" -> Volt.INSTANCE.fontManager.getSize(scaledSize, FontManager.Type.Poppins);
            default -> Volt.INSTANCE.fontManager.getSize(scaledSize, FontManager.Type.Inter);
        };
    }

    private String getIP() {
        if (mc.world == null) {
            return "NULL";
        } else {
            if (mc.isInSingleplayer()) {
                return "Singleplayer";
            } else {
                return mc.getCurrentServerEntry().address;
            }
        }
    }

    private String getFullName(Module m) {
        if (lowercase.getValue()) {
            if (m.getSuffix() == null) {
                return m.getName().toLowerCase();
            } else {
                switch (suffixMode.getMode()) {
                    case "Space":
                        return (m.getName() + " " + Formatting.GRAY + m.getSuffix()).toLowerCase();
                    case "-":
                        return (m.getName() + Formatting.GRAY + " - " + m.getSuffix()).toLowerCase();
                    case ">":
                        return (m.getName() + Formatting.GRAY + " > " + m.getSuffix()).toLowerCase();
                    case "[ ]":
                        return (m.getName() + Formatting.DARK_GRAY + " [" + Formatting.GRAY + m.getSuffix() + Formatting.DARK_GRAY + "]").toLowerCase();
                }
            }
        } else {
            if (m.getSuffix() == null) {
                return m.getName();
            } else {
                switch (suffixMode.getMode()) {
                    case "Space":
                        return m.getName() + " " + Formatting.GRAY + m.getSuffix();
                    case "-":
                        return m.getName() + Formatting.GRAY + " - " + m.getSuffix();
                    case ">":
                        return m.getName() + Formatting.GRAY + " > " + m.getSuffix();
                    case "[ ]":
                        return m.getName() + Formatting.DARK_GRAY + " [" + Formatting.GRAY + m.getSuffix() + Formatting.DARK_GRAY + "]";
                }
            }
        }
        return "";
    }

    private int applyWaveColor(int baseColor, double waveValue) {
        Color base = new Color(baseColor, true);
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        float brightness = Math.min(1f, Math.max(0f, (float) (hsb[2] * (0.6 + waveValue * 0.4))));
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], brightness);
        return (base.getAlpha() << 24) | (rgb & 0xFFFFFF);
    }

    public static int getAstolfo(int offset) {
        int i = (int) ((System.currentTimeMillis() / 11 + offset) % 360);
        i = (i > 180 ? 360 - i : i) + 180;
        return Color.HSBtoRGB(i / 360f, 0.55f, 1f);
    }

    private Color getThemeColor() {
        return new Color(150, 0, 255);
    }

    private Color getThemeColor(int variant) {
        return switch (variant) {
            case 1 -> new Color(200, 0, 255);
            case 2 -> new Color(100, 0, 255);
            default -> getThemeColor();
        };
    }

    private Color getCustomColor() {
        return customColor.getValue();
    }
}