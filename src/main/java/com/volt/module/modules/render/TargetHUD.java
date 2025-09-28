package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.Volt;
import com.volt.event.impl.render.EventRender2D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.utils.font.FontManager;
import com.volt.utils.font.fonts.FontRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TargetHUD extends Module {

    private static final int BOX_WIDTH = 220;
    private static final int BOX_HEIGHT = 96;
    private static final int PADDING = 10;
    private static final int HEAD_SIZE = 40;

    private static final Color BOX_COLOR = new Color(28, 28, 36);
    private static final Color BOX_OUTLINE = new Color(10, 10, 16);
    private static final Color HEALTH_BACKGROUND = new Color(20, 20, 28);
    private static final Color HEALTH_COLOR = new Color(170, 70, 255);
    private static final Color ABSORPTION_COLOR = new Color(208, 160, 255);
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFE7E4FF;
    private static final int TEXT_STATUS = 0xFFBBAAFF;
    private static final int TEXT_META = 0xFF9FA9D9;

    private FontRenderer interLarge;
    private FontRenderer interMedium;
    private FontRenderer interSmall;

    public TargetHUD() {
        super("Target HUD", "Displays information about the current combat target", -1, Category.RENDER);
    }

    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;

        PlayerEntity target = findTarget();
        if (target == null) {
            return;
        }

        DrawContext context = event.getContext();
        MatrixStack matrices = context.getMatrices();

        int x = 20;
        int y = event.getHeight() / 2 - BOX_HEIGHT / 2;

        matrices.push();
        matrices.translate(0, 0, 400);

        ensureFonts();

        context.fill(x, y, x + BOX_WIDTH, y + BOX_HEIGHT, BOX_COLOR.getRGB());
        context.drawBorder(x, y, BOX_WIDTH, BOX_HEIGHT, BOX_OUTLINE.getRGB());
        context.fill(x, y, x + 3, y + BOX_HEIGHT, HEALTH_COLOR.getRGB());

        int headX = x + PADDING;
        int headY = y + PADDING;
        renderPlayerHead(context, target, headX, headY, HEAD_SIZE);

        int textX = headX + HEAD_SIZE + 8;
        int textY = headY;

        String name = target.getName().getString();
        drawInter(interLarge, context, name, textX, textY, TEXT_PRIMARY);

        float playerTotalHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        float targetTotalHealth = target.getHealth() + target.getAbsorptionAmount();
        CombatStatus status = determineStatus(playerTotalHealth, targetTotalHealth);
        drawInter(interMedium, context, status.text(), textX, textY + 18, TEXT_STATUS);

        int barX = textX;
        int barY = headY + HEAD_SIZE + 4;
        int barWidth = BOX_WIDTH - (barX - x) - PADDING;
        renderHealthBar(context, target, barX, barY, barWidth, 10);

        String healthText = formatHealthValue(targetTotalHealth) + "/" + formatHealthValue(target.getMaxHealth());
        float healthTextWidth = getStringWidth(interMedium, healthText);
        int healthTextX = (int) (x + BOX_WIDTH - PADDING - healthTextWidth);
        float healthTextY = barY - 16;
        drawInter(interMedium, context, healthText, healthTextX, healthTextY, TEXT_SECONDARY);

        double distance = mc.player.distanceTo(target);
        String rangeMeta = String.format("%.1fm", distance);
        float rangeWidth = getStringWidth(interSmall, rangeMeta);
        float rangeHeight = getStringHeight(interSmall, rangeMeta);
        float rangeY = y + BOX_HEIGHT - PADDING - rangeHeight;
        drawInter(interSmall, context, rangeMeta, x + BOX_WIDTH - PADDING - rangeWidth, rangeY, TEXT_META);

        List<ItemStack> armorStacks = collectArmor(target);
        if (!armorStacks.isEmpty()) {
            int armorRowY = (int) (y + BOX_HEIGHT - PADDING - 18);
            int armorStartX = x + PADDING;
            for (ItemStack stack : armorStacks) {
                renderItemStack(context, stack, armorStartX, armorRowY);
                armorStartX += 20;
            }
        }

        matrices.pop();
    }

    private PlayerEntity findTarget() {
        if (mc.player == null || mc.world == null) {
            return null;
        }

        if (mc.targetedEntity instanceof PlayerEntity player && player.isAlive() && player != mc.player && !player.isSpectator()) {
            return player;
        }

        double range = 8.0;
        PlayerEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
            double distance = mc.player.distanceTo(player);
            if (distance <= range && distance < closestDistance) {
                closest = player;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private void renderHealthBar(DrawContext context, PlayerEntity target, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, HEALTH_BACKGROUND.getRGB());

        float maxHealth = Math.max(target.getMaxHealth(), 1f);
        float health = MathHelper.clamp(target.getHealth(), 0f, maxHealth);
        float absorption = Math.max(target.getAbsorptionAmount(), 0f);

        float healthPercent = MathHelper.clamp(health / maxHealth, 0f, 1f);
        int healthWidth = Math.round(width * healthPercent);
        if (healthWidth > 0) {
            context.fill(x, y, x + healthWidth, y + height, HEALTH_COLOR.getRGB());
        }

        if (absorption > 0f) {
            float absorptionPercent = MathHelper.clamp((health + absorption) / maxHealth, 0f, 1f);
            int absorptionWidth = Math.round(width * absorptionPercent);
            int overlayWidth = Math.max(0, absorptionWidth - healthWidth);
            if (overlayWidth > 0) {
                context.fill(x + healthWidth, y, x + healthWidth + overlayWidth, y + height, ABSORPTION_COLOR.getRGB());
            }
        }

        context.drawBorder(x, y, width, height, BOX_OUTLINE.getRGB());
    }

    private void renderPlayerHead(DrawContext context, PlayerEntity player, int x, int y, int size) {
        Identifier texture = resolveSkin(player);
        if (texture == null) {
            return;
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        float scale = size / 8.0f;
        matrices.scale(scale, scale, 1f);

        RenderSystem.enableBlend();
        context.drawTexture(texture, 0, 0, 8, 8, 8, 8, 64, 64);
        context.drawTexture(texture, 0, 0, 40, 8, 8, 8, 64, 64);
        RenderSystem.disableBlend();

        matrices.pop();
    }

    private void drawInter(FontRenderer font, DrawContext context, String text, float x, float y, int color) {
        if (font != null) {
            font.drawString(context.getMatrices(), text, x, y, new Color(color, true));
        } else {
            context.drawTextWithShadow(mc.textRenderer, text, (int) x, (int) y, color);
        }
    }

    private float getStringWidth(FontRenderer font, String text) {
        return font != null ? font.getStringWidth(text) : mc.textRenderer.getWidth(text);
    }

    private float getStringHeight(FontRenderer font, String text) {
        if (font != null) {
            return font.getStringHeight(text);
        }
        return mc.textRenderer.fontHeight;
    }

    private String formatHealthValue(float value) {
        if (Math.abs(value - Math.round(value)) < 0.05f) {
            return Integer.toString(Math.round(value));
        }
        return String.format("%.1f", value);
    }

    private void ensureFonts() {
        if (interLarge != null && interMedium != null && interSmall != null) {
            return;
        }

        if (Volt.INSTANCE == null || Volt.INSTANCE.getFontManager() == null) {
            return;
        }

        FontManager fontManager = Volt.INSTANCE.getFontManager();
        if (interLarge == null) {
            interLarge = fontManager.getSize(18, FontManager.Type.Inter);
        }
        if (interMedium == null) {
            interMedium = fontManager.getSize(16, FontManager.Type.Inter);
        }
        if (interSmall == null) {
            interSmall = fontManager.getSize(14, FontManager.Type.Inter);
        }
    }

    private void renderItemStack(DrawContext context, ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        context.drawItem(stack, x, y, 0);
        context.drawItemInSlot(mc.textRenderer, stack, x, y);
    }

    private List<ItemStack> collectArmor(PlayerEntity target) {
        List<ItemStack> armor = new ArrayList<>();
        target.getArmorItems().forEach(armor::add);
        Collections.reverse(armor);
        armor.removeIf(ItemStack::isEmpty);
        if (armor.size() > 6) {
            armor = new ArrayList<>(armor.subList(0, 6));
        }
        return armor;
    }

    private Identifier resolveSkin(PlayerEntity player) {
        SkinTextures textures;
        if (player instanceof AbstractClientPlayerEntity clientPlayer) {
            textures = clientPlayer.getSkinTextures();
        } else {
            textures = DefaultSkinHelper.getSkinTextures(player.getUuid());
        }
        return textures.texture();
    }

    private CombatStatus determineStatus(float playerTotal, float targetTotal) {
        float diff = playerTotal - targetTotal;
        if (diff > 2.0f) {
            return new CombatStatus("Advantage", 0xFF7CFF9A);
        } else if (diff < -2.0f) {
            return new CombatStatus("Danger", 0xFFFF6666);
        }
        return new CombatStatus("Even", 0xFFFFE27A);
    }

    private record CombatStatus(String text, int color) {
    }
}
