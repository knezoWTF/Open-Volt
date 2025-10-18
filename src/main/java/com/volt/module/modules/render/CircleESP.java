package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.ModeSetting;
import com.volt.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;

public final class CircleESP extends Module {
    private final ModeSetting targets = new ModeSetting("Targets", "Players", "Players", "Living");
    private final NumberSetting heightMin = new NumberSetting("Height Min", 0.1, 2.5, 0.2, 0.05);
    private final NumberSetting heightMax = new NumberSetting("Height Max", 0.2, 3.0, 1.4, 0.05);
    private final NumberSetting radius = new NumberSetting("Radius", 0.2, 2.0, 0.7, 0.05);
    private final NumberSetting thickness = new NumberSetting("Thickness", 0.01, 0.3, 0.06, 0.005);
    private final NumberSetting speed = new NumberSetting("Speed", 0.1, 5.0, 1.6, 0.1);
    private final NumberSetting segments = new NumberSetting("Segments", 16, 128, 64, 1);
    private final NumberSetting sideFade = new NumberSetting("Side Fade", 0.0, 1.0, 0.5, 0.05);
    private final ColorSetting color = new ColorSetting("Color", new Color(100, 255, 255, 200));

    public CircleESP() {
        super("Circle ESP", "Animated ring around players", -1, Category.RENDER);
        addSettings(targets, heightMin, heightMax, radius, thickness, speed, segments, sideFade, color);
    }

    @EventHandler
    private void onRender3D(EventRender3D event) {
        if (isNull()) return;

        MatrixStack matrices = event.getMatrixStack();
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        float r = color.getValue().getRed() / 255.0f;
        float g = color.getValue().getGreen() / 255.0f;
        float b = color.getValue().getBlue() / 255.0f;

        float minH = heightMin.getValueFloat();
        float maxH = heightMax.getValueFloat();
        float radius = this.radius.getValueFloat();
        float thick = thickness.getValueFloat();
        int segments = Math.max(8, this.segments.getValueInt());

        double time = (System.currentTimeMillis() / 1000.0) * speed.getValue();
        double phase = (Math.sin(time) * 0.5) + 0.5;
        float y = (float) (minH + (maxH - minH) * phase);
        float baseAlpha = color.getValue().getAlpha() / 255.0f;

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        boolean hasVertices = false;

        for (var entity : mc.world.getEntities()) {
            if (!should(entity)) continue;
            hasVertices = true;
            Vec3d base = entity.getPos();

            float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
            double cx = (entity.prevX + (base.getX() - entity.prevX) * tickDelta) - cam.x;
            double cy = (entity.prevY + (base.getY() - entity.prevY) * tickDelta) - cam.y + y;
            double cz = (entity.prevZ + (base.getZ() - entity.prevZ) * tickDelta) - cam.z;

            for (int i = 0; i < segments; i++) {
                double a0 = (i / (double) segments) * Math.PI * 2.0;
                double a1 = ((i + 1) / (double) segments) * Math.PI * 2.0;
                double x0 = Math.cos(a0), z0 = Math.sin(a0);
                double x1 = Math.cos(a1), z1 = Math.sin(a1);

                double aMid = (a0 + a1) * 0.5;
                float sideFactor = (float) Math.abs(Math.sin(aMid));
                float aSeg = baseAlpha * (1.0f - (float) sideFade.getValue() * sideFactor);
                float aOut = aSeg * 0.2f;

                float x0i = (float) (cx + x0 * radius);
                float z0i = (float) (cz + z0 * radius);
                float x1i = (float) (cx + x1 * radius);
                float z1i = (float) (cz + z1 * radius);
                float x0o = (float) (cx + x0 * (radius + thick));
                float z0o = (float) (cz + z0 * (radius + thick));
                float x1o = (float) (cx + x1 * (radius + thick));
                float z1o = (float) (cz + z1 * (radius + thick));

                buffer.vertex(posMatrix, x0i, (float) cy, z0i).color(r, g, b, aSeg);
                buffer.vertex(posMatrix, x1i, (float) cy, z1i).color(r, g, b, aSeg);
                buffer.vertex(posMatrix, x1o, (float) cy, z1o).color(r, g, b, aOut);

                buffer.vertex(posMatrix, x0i, (float) cy, z0i).color(r, g, b, aSeg);
                buffer.vertex(posMatrix, x1o, (float) cy, z1o).color(r, g, b, aOut);
                buffer.vertex(posMatrix, x0o, (float) cy, z0o).color(r, g, b, aOut);
            }
        }

        if (hasVertices) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.enableDepthTest();
    }

    private boolean should(net.minecraft.entity.Entity entity) {
        if (entity == mc.player) return entity instanceof PlayerEntity && targets.isMode("Players");
        if (targets.isMode("Players")) return entity instanceof PlayerEntity;
        return entity instanceof LivingEntity;
    }
}


