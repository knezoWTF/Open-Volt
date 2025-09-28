package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.render.EventRender2D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.render.TextureRenderer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public final class ArrowESP extends Module {
    private static final Identifier ARROW_TEXTURE = Identifier.of("volt", "imgs/triangle.png");
    private final BooleanSetting showSelf = new BooleanSetting("Show Self", false);
    private final NumberSetting range = new NumberSetting("Range", 10.0D, 200.0D, 120.0D, 5.0D);
    private final NumberSetting size = new NumberSetting("Size", 8.0D, 64.0D, 24.0D, 1.0D);
    private final NumberSetting offset = new NumberSetting("Offset", 0.0D, 64.0D, 28.0D, 1.0D);
    private final ColorSetting color = new ColorSetting("Color", new Color(255, 255, 255, 200));

    public ArrowESP() {
        super("Arrow ESP", "Displays arrows pointing to players", -1, Category.RENDER);
        addSettings(showSelf, range, size, offset, color);
    }

    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (isNull()) return;

        DrawContext context = event.getContext();
        MatrixStack matrices = context.getMatrices();
        int width = event.getWidth();
        int height = event.getHeight();
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;

        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        float cameraYaw = mc.gameRenderer.getCamera().getYaw();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, ARROW_TEXTURE);

        Color arrowColor = color.getValue();
        int baseR = arrowColor.getRed();
        int baseG = arrowColor.getGreen();
        int baseB = arrowColor.getBlue();
        int baseA = arrowColor.getAlpha();
        float sizeValue = size.getValueFloat();
        float offsetValue = offset.getValueFloat();
        double maxRange = range.getValue();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!shouldRender(player)) continue;
            double distanceSq = mc.player.squaredDistanceTo(player);
            if (distanceSq > maxRange * maxRange) continue;

            double interpolatedX = player.prevX + (player.getX() - player.prevX) * tickDelta;
            double interpolatedZ = player.prevZ + (player.getZ() - player.prevZ) * tickDelta;

            double dx = interpolatedX - cameraPos.x;
            double dz = interpolatedZ - cameraPos.z;

            double planar = Math.sqrt(dx * dx + dz * dz);
            if (planar < 1.0E-6) continue;

            float yawToPlayer = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0F - cameraYaw);

            double distance = Math.sqrt(distanceSq);
            float distanceFactor = (float) Math.min(distance / maxRange, 1.0D);
            float alphaScale = 1.0f - distanceFactor * 0.6f;
            int arrowAlpha = Math.max(24, Math.min(255, (int) (baseA * alphaScale)));

            matrices.push();
            matrices.translate(centerX, centerY, 0.0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yawToPlayer));
            matrices.translate(0.0, -(offsetValue + distanceFactor * offsetValue), 0.0);

            int baseColorPacked = (baseA << 24) | (baseR << 16) | (baseG << 8) | baseB;
            int packed = (arrowAlpha << 24) | (baseColorPacked & 0x00FFFFFF);
            TextureRenderer.drawCenteredQuad(matrices, ARROW_TEXTURE, sizeValue, sizeValue, packed);
            matrices.pop();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private boolean shouldRender(PlayerEntity player) {
        if (player == mc.player && !showSelf.getValue()) return false;
        return !player.isSpectator();
    }
}
