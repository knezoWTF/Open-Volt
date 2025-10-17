package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.ModeSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.render.font.util.RendererUtils;
import com.volt.utils.render.RenderUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public final class EntityESP extends Module {

    private static float lastLineWidth = -1.0f;
    private final ModeSetting renderMode = new ModeSetting("Mode", "Outline", "Outline", "Filled", "Both");
    private final NumberSetting lineWidth = new NumberSetting("Line Width", 1.0, 5.0, 2.0, 0.1);
    private final ColorSetting hostileColor = new ColorSetting("Hostile Color", new Color(255, 50, 50, 150));
    private final ColorSetting passiveColor = new ColorSetting("Passive Color", new Color(50, 255, 50, 150));
    private final ColorSetting villagerColor = new ColorSetting("Villager Color", new Color(50, 50, 255, 150));
    private final ColorSetting otherColor = new ColorSetting("Other Color", new Color(255, 255, 50, 150));
    private final BooleanSetting showHostile = new BooleanSetting("Show Hostile", true);
    private final BooleanSetting showPassive = new BooleanSetting("Show Passive", true);
    private final BooleanSetting showVillagers = new BooleanSetting("Show Villagers", true);
    private final BooleanSetting showOther = new BooleanSetting("Show Other", false);
    private final NumberSetting range = new NumberSetting("Range", 10, 200, 50, 5);

    public EntityESP() {
        super("Entity ESP", "ESP around animals mobs ect", -1, Category.RENDER);
        this.addSettings(renderMode, lineWidth, hostileColor, passiveColor, villagerColor, otherColor,
                showHostile, showPassive, showVillagers, showOther, range);
    }

    @EventHandler
    private void onEventRender3D(EventRender3D event) {
        if (isNull()) return;

        MatrixStack matrices = event.getMatrixStack();
        float partialTicks = mc.getRenderTickCounter().getTickDelta(true);

        RendererUtils.setupRender();

        RenderSystem.disableDepthTest();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        float lineWidthValue = Math.max(0.1f, Math.min(10.0f, (float) lineWidth.getValue()));
        if (Math.abs(lineWidthValue - lastLineWidth) > 0.01f) {
            GL11.glLineWidth(lineWidthValue);
            lastLineWidth = lineWidthValue;
        }

        for (Entity entity : mc.world.getEntities()) {
            if (!shouldRender(entity)) continue;

            renderEntityESP(matrices, entity, partialTicks);
        }

        GL11.glLineWidth(1.0f);

        RenderSystem.enableDepthTest();

        RendererUtils.endRender();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private boolean shouldRender(Entity entity) {
        if (!(entity instanceof LivingEntity)) return false;
        if (entity == mc.player) return false;
        if (mc.player.distanceTo(entity) > range.getValue()) return false;

        return switch (entity) {
            case PlayerEntity ignored1 -> false;
            case VillagerEntity ignored -> showVillagers.getValue();
            case HostileEntity ignored -> showHostile.getValue();
            case PassiveEntity ignored -> showPassive.getValue();
            default -> showOther.getValue();
        };
    }

    private Color getEntityColor(Entity entity) {
        return switch (entity) {
            case VillagerEntity ignored -> villagerColor.getValue();
            case HostileEntity ignored -> hostileColor.getValue();
            case PassiveEntity ignored -> passiveColor.getValue();
            case null, default -> otherColor.getValue();
        };
    }

    private void renderEntityESP(MatrixStack matrices, Entity entity, float partialTicks) {
        Vec3d entityPos = getInterpolatedPos(entity, partialTicks);
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        double x = entityPos.x - cameraPos.x;
        double y = entityPos.y - cameraPos.y;
        double z = entityPos.z - cameraPos.z;

        Box boundingBox = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());

        matrices.push();
        matrices.translate(x, y, z);

        Color espColor = getEntityColor(entity);

        if (renderMode.isMode("Outline") || renderMode.isMode("Both")) {
            RenderUtils.renderOutline(matrices, boundingBox, espColor);
        }

        if (renderMode.isMode("Filled") || renderMode.isMode("Both")) {
            Color fillColor = new Color(espColor.getRed(), espColor.getGreen(), espColor.getBlue(), Math.min(80, espColor.getAlpha()));
            RenderUtils.renderFilled(matrices, boundingBox, fillColor);
        }

        matrices.pop();
    }

    private Vec3d getInterpolatedPos(Entity entity, float partialTicks) {
        double x = entity.prevX + (entity.getX() - entity.prevX) * partialTicks;
        double y = entity.prevY + (entity.getY() - entity.prevY) * partialTicks;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * partialTicks;
        return new Vec3d(x, y, z);
    }
}