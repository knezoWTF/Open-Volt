package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Trajectory extends Module {

    private final BooleanSetting showPearls = new BooleanSetting("Show Pearls", true);
    private final BooleanSetting showBows = new BooleanSetting("Show Bows", true);
    private final BooleanSetting showLandingPoint = new BooleanSetting("Show Landing Point", true);

    public Trajectory() {
        super("Trajectory", "Shows projectile trajectories", Category.RENDER);
        addSettings(showPearls, showBows, showLandingPoint);
    }

    @EventHandler
    private void onRender3D(EventRender3D event) {
        if (Objects.isNull(mc.player) || Objects.isNull(mc.world)) return;

        ItemStack heldItem = mc.player.getMainHandStack();
        if (heldItem.isEmpty()) return;

        Item item = heldItem.getItem();

        if (item == Items.ENDER_PEARL && showPearls.getValue()) {
            renderTrajectory(event.getMatrixStack(), 1.5, 0.03);
        } else if (item == Items.BOW && showBows.getValue()) {
            double bowVelocity = calculateBowVelocity(heldItem);
            if (bowVelocity > 0) {
                renderTrajectory(event.getMatrixStack(), bowVelocity, 0.05);
            }
        } else if (item == Items.CROSSBOW && showBows.getValue()) {
            renderTrajectory(event.getMatrixStack(), 3.15, 0.05);
        }
    }

    private void renderTrajectory(MatrixStack matrices, double velocity, double gravity) {
        Vec3d startPos =
                mc.player.getCameraPosVec(1.0f).add(mc.player.getRotationVector().multiply(0.16));

        float pitch = mc.player.getPitch();
        float yaw = mc.player.getYaw();

        float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        float g = -MathHelper.sin(pitch * 0.017453292F);
        float h = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
        Vec3d motion = new Vec3d(f, g, h).normalize().multiply(velocity);

        List<Vec3d> points = new ArrayList<>();
        Vec3d pos = startPos;

        for (int i = 0; i < 100; i++) {
            points.add(pos);

            Vec3d nextPos = pos.add(motion);
            HitResult hit =
                    mc.world.raycast(
                            new RaycastContext(
                                    pos,
                                    nextPos,
                                    RaycastContext.ShapeType.COLLIDER,
                                    RaycastContext.FluidHandling.NONE,
                                    mc.player));

            if (hit.getType() != HitResult.Type.MISS) {
                break;
            }

            pos = nextPos;
            motion = motion.multiply(0.99).add(0, -gravity, 0);
        }

        if (points.size() < 2) return;

        matrices.push();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer =
                tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        for (Vec3d point : points) {
            Vec3d relativePos = point.subtract(cameraPos);
            buffer
                    .vertex(
                            matrices.peek().getPositionMatrix(),
                            (float) relativePos.x,
                            (float) relativePos.y,
                            (float) relativePos.z)
                    .color(255, 100, 100, 200);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        if (showLandingPoint.getValue()) {
            renderLandingPoint(matrices, points.getLast(), cameraPos);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    private double calculateBowVelocity(ItemStack bowStack) {
        if (mc.player.isUsingItem() && mc.player.getActiveItem() == bowStack) {
            int useTime = mc.player.getItemUseTime();
            float progress = Math.min((float) useTime / 20.0f, 1.0f);

            float velocity = (progress * progress + progress * 2.0f) / 3.0f;
            if (velocity > 1.0f) velocity = 1.0f;

            return velocity * 3.0f;
        }
        return 0;
    }

    private void renderLandingPoint(MatrixStack matrices, Vec3d landingPos, Vec3d cameraPos) {
        Vec3d relativePos = landingPos.subtract(cameraPos);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer =
                tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float size = 0.2f;
        float x = (float) relativePos.x;
        float y = (float) relativePos.y;
        float z = (float) relativePos.z;

        buffer.vertex(matrices.peek().getPositionMatrix(), x - size, y, z - size).color(255, 255, 0, 200);
        buffer.vertex(matrices.peek().getPositionMatrix(), x + size, y, z - size).color(255, 255, 0, 200);
        buffer.vertex(matrices.peek().getPositionMatrix(), x + size, y, z + size).color(255, 255, 0, 200);
        buffer.vertex(matrices.peek().getPositionMatrix(), x - size, y, z + size).color(255, 255, 0, 200);

        buffer
                .vertex(matrices.peek().getPositionMatrix(), x - size, y + size * 2, z - size)
                .color(255, 255, 0, 200);
        buffer
                .vertex(matrices.peek().getPositionMatrix(), x - size, y + size * 2, z + size)
                .color(255, 255, 0, 200);
        buffer
                .vertex(matrices.peek().getPositionMatrix(), x + size, y + size * 2, z + size)
                .color(255, 255, 0, 200);
        buffer
                .vertex(matrices.peek().getPositionMatrix(), x + size, y + size * 2, z - size)
                .color(255, 255, 0, 200);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}
