package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.render.font.util.RendererUtils;
import com.volt.utils.friend.FriendManager;
import com.volt.utils.render.RenderUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public final class SkeletonESP extends Module {

    private final ColorSetting color = new ColorSetting("Color", new Color(255, 255, 255, 200));
    private final BooleanSetting teamCheck = new BooleanSetting("Team Check", false);
    private final NumberSetting range = new NumberSetting("Range", 10, 200, 32, 5);

    public SkeletonESP() {
        super("Skeleton ESP", "Renders player skeletons", -1, Category.RENDER);
        this.addSettings(color, teamCheck, range);
    }

    @EventHandler
    private void onEventRender3D(EventRender3D event) {
        if (isNull()) return;

        MatrixStack matrices = event.getMatrixStack();
        float partialTicks = mc.getRenderTickCounter().getTickDelta(true);

        try {
            RendererUtils.setupRender();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(2.5f);

            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!shouldRender(player)) continue;

                renderSkeleton(matrices, player, partialTicks);
            }

            GL11.glLineWidth(1.0f);

            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        } catch (Exception ignored) {
        } finally {
            try {
                RendererUtils.endRender();
            } catch (Exception ignored) {
            }
        }
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
        if (player == mc.player) return false;
        if (mc.player.distanceTo(player) > range.getValue()) return false;
        return !teamCheck.getValue() || !isTeammate(player);
    }

    private boolean isTeammate(PlayerEntity player) {
        if (mc.player.getScoreboardTeam() == null || player.getScoreboardTeam() == null) {
            return false;
        }
        return mc.player.getScoreboardTeam().equals(player.getScoreboardTeam());
    }

    private void renderSkeleton(MatrixStack matrices, PlayerEntity player, float partialTicks) {
        Vec3d playerPos = getInterpolatedPos(player, partialTicks);
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();

        double x = playerPos.x - cameraPos.x;
        double y = playerPos.y - cameraPos.y;
        double z = playerPos.z - cameraPos.z;

        matrices.push();
        matrices.translate(x, y, z);

        float bodyYaw = player.prevBodyYaw + (player.bodyYaw - player.prevBodyYaw) * partialTicks;
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-bodyYaw));

        Color skeletonColor;
        if (FriendManager.isFriend(player.getUuid())) {
            skeletonColor = new Color(128, 0, 128, this.color.getValue().getAlpha());
        } else {
            skeletonColor = this.color.getValue();
        }

        Vec3d head = new Vec3d(0, 1.62, 0);
        Vec3d neck = new Vec3d(0, 1.4, 0);
        RenderUtils.renderLine(matrices, head, neck, skeletonColor);

        Vec3d chest = new Vec3d(0, 1.1, 0);
        Vec3d waist = new Vec3d(0, 0.6, 0);
        RenderUtils.renderLine(matrices, neck, chest, skeletonColor);
        RenderUtils.renderLine(matrices, chest, waist, skeletonColor);

        Vec3d leftShoulder = new Vec3d(-0.3, 1.35, 0);
        Vec3d rightShoulder = new Vec3d(0.3, 1.35, 0);
        Vec3d leftHand = new Vec3d(-0.3, 0.9, 0);
        Vec3d rightHand = new Vec3d(0.3, 0.9, 0);

        RenderUtils.renderLine(matrices, chest, leftShoulder, skeletonColor);
        RenderUtils.renderLine(matrices, chest, rightShoulder, skeletonColor);

        RenderUtils.renderLine(matrices, leftShoulder, leftHand, skeletonColor);
        RenderUtils.renderLine(matrices, rightShoulder, rightHand, skeletonColor);

        Vec3d leftHip = new Vec3d(-0.15, 0.6, 0);
        Vec3d rightHip = new Vec3d(0.15, 0.6, 0);
        Vec3d leftFoot = new Vec3d(-0.15, 0, 0);
        Vec3d rightFoot = new Vec3d(0.15, 0, 0);

        RenderUtils.renderLine(matrices, waist, leftHip, skeletonColor);
        RenderUtils.renderLine(matrices, waist, rightHip, skeletonColor);

        RenderUtils.renderLine(matrices, leftHip, leftFoot, skeletonColor);
        RenderUtils.renderLine(matrices, rightHip, rightFoot, skeletonColor);

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