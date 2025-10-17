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
import com.volt.utils.friend.FriendManager;
import com.volt.utils.render.RenderUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public final class PlayerESP extends Module {

    private static float lastLineWidth = -1.0f;
    private final ModeSetting renderMode = new ModeSetting("Mode", "Outline", "Outline", "Filled", "Both");
    private final NumberSetting lineWidth = new NumberSetting("Line Width", 1.0, 5.0, 2.0, 0.1);
    private final ColorSetting color = new ColorSetting("Color", new Color(255, 100, 100, 150));
    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", true);
    private final BooleanSetting showSelf = new BooleanSetting("Show Self", false);
    private final BooleanSetting teamCheck = new BooleanSetting("Team Check", false);
    private final NumberSetting range = new NumberSetting("Range", 10, 200, 100, 5);

    public PlayerESP() {
        super("Player ESP", "Renders sleek outlines around players", -1, Category.RENDER);
        this.addSettings(renderMode, lineWidth, color, throughWalls, showSelf, teamCheck, range);
    }

    @EventHandler
    private void onEventRender3D(EventRender3D event) {
        if (isNull()) return;

        MatrixStack matrices = event.getMatrixStack();
        float partialTicks = mc.getRenderTickCounter().getTickDelta(true);

        RendererUtils.setupRender();

        if (throughWalls.getValue()) {
            RenderSystem.disableDepthTest();
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        float lineWidthValue = Math.max(0.1f, Math.min(10.0f, (float) lineWidth.getValue()));
        if (Math.abs(lineWidthValue - lastLineWidth) > 0.01f) {
            GL11.glLineWidth(lineWidthValue);
            lastLineWidth = lineWidthValue;
        }

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!shouldRender(player)) continue;

            renderPlayerESP(matrices, player, partialTicks);
        }

        GL11.glLineWidth(1.0f);

        if (throughWalls.getValue()) {
            RenderSystem.enableDepthTest();
        }

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


    private boolean shouldRender(PlayerEntity player) {
        if (player == mc.player && !showSelf.getValue()) return false;
        if (mc.player.distanceTo(player) > range.getValue()) return false;
        return !teamCheck.getValue() || !isTeammate(player);
    }

    private boolean isTeammate(PlayerEntity player) {
        if (mc.player.getScoreboardTeam() == null || player.getScoreboardTeam() == null) {
            return false;
        }
        return mc.player.getScoreboardTeam().equals(player.getScoreboardTeam());
    }

    private void renderPlayerESP(MatrixStack matrices, PlayerEntity player, float partialTicks) {
        Vec3d playerPos = getInterpolatedPos(player, partialTicks);
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        double x = playerPos.x - cameraPos.x;
        double y = playerPos.y - cameraPos.y;
        double z = playerPos.z - cameraPos.z;

        Box boundingBox = new Box(-0.3, 0, -0.3, 0.3, 1.8, 0.3);

        matrices.push();
        matrices.translate(x, y, z);

        Color espColor;
        if (FriendManager.isFriend(player.getUuid())) {
            espColor = new Color(128, 0, 128, this.color.getValue().getAlpha());
        } else {
            espColor = this.color.getValue();
        }

        if (renderMode.isMode("Outline") || renderMode.isMode("Both")) {
            RenderUtils.renderOutline(matrices, boundingBox, espColor);
        }

        if (renderMode.isMode("Filled") || renderMode.isMode("Both")) {
            Color fillColor = new Color(espColor.getRed(), espColor.getGreen(), espColor.getBlue(), Math.min(80, espColor.getAlpha()));
            RenderUtils.renderFilled(matrices, boundingBox, fillColor);
        }

        matrices.pop();
    }

    private Vec3d getInterpolatedPos(PlayerEntity player, float partialTicks) {
        if (player == mc.player) {
            return mc.gameRenderer.getCamera().getPos();
        }

        double x = player.prevX + (player.getX() - player.prevX) * partialTicks;
        double y = player.prevY + (player.getY() - player.prevY) * partialTicks;
        double z = player.prevZ + (player.getZ() - player.prevZ) * partialTicks;
        return new Vec3d(x, y, z);
    }
}