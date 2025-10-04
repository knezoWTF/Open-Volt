package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.ModeSetting;
import com.volt.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

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
    private void onRender3D(EventRender3D e) {
        if (isNull()) return;

        MatrixStack matrices = e.getMatrixStack();
        Matrix4f m = matrices.peek().getPositionMatrix();
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
        float rad = radius.getValueFloat();
        float thick = thickness.getValueFloat();
        int segs = Math.max(8, segments.getValueInt());

        double t = (System.currentTimeMillis() / 1000.0) * speed.getValue();
        double phase = (Math.sin(t) * 0.5) + 0.5;
        float y = (float) (minH + (maxH - minH) * phase);
        float baseAlpha = color.getValue().getAlpha() / 255.0f;

BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

boolean hasVertices = false;

for (var ent : mc.world.getEntities()) {
    if (!should(ent)) continue;
    hasVertices = true; 
    Vec3d base = ent.getPos();
    double cx = base.x - cam.x;
    double cy = base.y - cam.y + y;
    double cz = base.z - cam.z;

    for (int i = 0; i < segs; i++) {
        double a0 = (i / (double) segs) * Math.PI * 2.0;
        double a1 = ((i + 1) / (double) segs) * Math.PI * 2.0;
        double x0 = Math.cos(a0), z0 = Math.sin(a0);
        double x1 = Math.cos(a1), z1 = Math.sin(a1);

        double aMid = (a0 + a1) * 0.5;
        float sideFactor = (float) Math.abs(Math.sin(aMid));
        float aSeg = baseAlpha * (1.0f - (float) sideFade.getValue() * sideFactor);
        float aOut = aSeg * 0.2f;

        float x0i = (float) (cx + x0 * rad);
        float z0i = (float) (cz + z0 * rad);
        float x1i = (float) (cx + x1 * rad);
        float z1i = (float) (cz + z1 * rad);
        float x0o = (float) (cx + x0 * (rad + thick));
        float z0o = (float) (cz + z0 * (rad + thick));
        float x1o = (float) (cx + x1 * (rad + thick));
        float z1o = (float) (cz + z1 * (rad + thick));

        buf.vertex(m, x0i, (float) cy, z0i).color(r, g, b, aSeg);
        buf.vertex(m, x1i, (float) cy, z1i).color(r, g, b, aSeg);
        buf.vertex(m, x1o, (float) cy, z1o).color(r, g, b, aOut);

        buf.vertex(m, x0i, (float) cy, z0i).color(r, g, b, aSeg);
        buf.vertex(m, x1o, (float) cy, z1o).color(r, g, b, aOut);
        buf.vertex(m, x0o, (float) cy, z0o).color(r, g, b, aOut);
    }
}

if (hasVertices) {
    BufferRenderer.drawWithGlobalProgram(buf.end());
}

RenderSystem.enableDepthTest();
    }

    private boolean should(net.minecraft.entity.Entity e) {
        if (e == mc.player) return e instanceof PlayerEntity && targets.isMode("Players");
        if (targets.isMode("Players")) return e instanceof PlayerEntity;
        return e instanceof LivingEntity;
    }
}


