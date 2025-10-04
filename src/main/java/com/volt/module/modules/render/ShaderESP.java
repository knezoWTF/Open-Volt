package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.ModeSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public final class ShaderESP extends Module {

    private final ModeSetting targets = new ModeSetting("Targets", "Players", "Players", "Living", "All");
    private final ColorSetting color = new ColorSetting("Color", new Color(0, 255, 255, 200));
    private final BooleanSetting showSelf = new BooleanSetting("Show Self", false);

    public ShaderESP() {
        super("Shader ESP", "Chams pretty much", -1, Category.RENDER);
        addSettings(targets, color, showSelf);
    }

    @EventHandler
    private void onRender3D(EventRender3D event) {
        if (isNull()) return;

        MatrixStack matrices = event.getMatrixStack();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);

        RenderSystem.disableDepthTest();

        OutlineVertexConsumerProvider outlines = mc.getBufferBuilders().getOutlineVertexConsumers();
        Color c = color.getValue();
        outlines.setColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        for (Entity e : mc.world.getEntities()) {
            if (!shouldRender(e)) continue;
            renderEntityWithProvider(e, tickDelta, matrices, outlines, 15728880);
        }
        outlines.draw();

        RenderSystem.enableDepthTest();
    }

    private boolean shouldRender(Entity entity) {
        if (!showSelf.getValue() && entity == mc.player) return false;
        if (targets.isMode("Players")) return entity instanceof PlayerEntity;
        if (targets.isMode("Living")) return entity instanceof LivingEntity;
        return true;
    }


    private void renderEntityWithProvider(Entity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, int light) {
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double x = entity.prevX + (entity.getX() - entity.prevX) * tickDelta - cam.x;
        double y = entity.prevY + (entity.getY() - entity.prevY) * tickDelta - cam.y;
        double z = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta - cam.z;
        mc.getEntityRenderDispatcher().render(entity, x, y, z, entity.getYaw(tickDelta), tickDelta, matrices, provider, light);
    }
}


