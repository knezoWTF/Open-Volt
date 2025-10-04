package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.player.TickEvent;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.ModeSetting;
import com.volt.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

public final class JumpCircles extends Module {

    private final ModeSetting targets = new ModeSetting("Targets", "Self", "Self", "Players");
    private final NumberSetting duration = new NumberSetting("Duration", 0.2, 3.0, 1.0, 0.05);
    private final NumberSetting maxRadius = new NumberSetting("Max Radius", 0.3, 5.0, 2.2, 0.05);
    private final NumberSetting thickness = new NumberSetting("Thickness", 0.01, 0.3, 0.06, 0.005);
    private final NumberSetting segments = new NumberSetting("Segments", 12, 128, 48, 1);
    private final ColorSetting color = new ColorSetting("Color", new Color(255, 255, 255, 200));
    private final BooleanSetting glow = new BooleanSetting("Glow", true);
    private final NumberSetting glowIntensity = new NumberSetting("Glow Intensity", 0.1, 2.0, 0.8, 0.05);
    private final NumberSetting glowRadius = new NumberSetting("Glow Radius", 0.1, 1.0, 0.3, 0.05);
    private final Deque<Ring> rings = new ArrayDeque<>();
    private boolean wasOnGround = true;
    public JumpCircles() {
        super("Jump Circles", "Animated ring displayed when jumping", -1, Category.RENDER);
        addSettings(targets, duration, maxRadius, thickness, segments, color, glow, glowIntensity, glowRadius);
    }

    private static void addTri(BufferBuilder buffer, Matrix4f matrix, Vec3d a, Vec3d b, Vec3d c,
                               float r, float g, float bl, float al, Vec3d cam) {
        buffer.vertex(matrix, (float) (a.x - cam.x), (float) (a.y - cam.y), (float) (a.z - cam.z)).color(r, g, bl, al);
        buffer.vertex(matrix, (float) (b.x - cam.x), (float) (b.y - cam.y), (float) (b.z - cam.z)).color(r, g, bl, al);
        buffer.vertex(matrix, (float) (c.x - cam.x), (float) (c.y - cam.y), (float) (c.z - cam.z)).color(r, g, bl, al);
    }

    @EventHandler
    private void onTick(TickEvent e) {
        if (isNull()) return;
        long now = System.currentTimeMillis();
        long life = (long) (duration.getValue() * 1000.0);

        if (mc.player != null) {
            boolean onGround = mc.player.isOnGround();
            if (wasOnGround && !onGround) {
                if (targets.isMode("Self") || targets.isMode("Players")) {
                    rings.addLast(new Ring(mc.player.getPos(), now, true));
                }
            }
            wasOnGround = onGround;
        }

        if (targets.isMode("Players")) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player) continue;
                if (p.isOnGround() || p.prevY == p.getY()) continue;
                rings.addLast(new Ring(p.getPos(), now, false));
            }
        }

        while (!rings.isEmpty() && now - rings.peekFirst().startTime > life) rings.removeFirst();
    }

    @EventHandler
    private void onRender3D(EventRender3D event) {
        if (isNull()) return;

        MatrixStack matrices = event.getMatrixStack();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        long now = System.currentTimeMillis();
        long life = (long) (duration.getValue() * 1000.0);
        float maxR = maxRadius.getValueFloat();
        float thick = thickness.getValueFloat();
        int segs = Math.max(8, segments.getValueInt());
        Color c = color.getValue();
        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;
        float aBase = c.getAlpha() / 255.0f;

        if (glow.getValue()) {
            renderRingsWithGlow(matrix, cam, now, life, maxR, thick, segs, r, g, b, aBase);
        } else {
            renderRingsNormal(matrix, cam, now, life, maxR, thick, segs, r, g, b, aBase);
        }

        RenderSystem.enableDepthTest();
    }

    private void renderRingsNormal(Matrix4f matrix, Vec3d cam, long now, long life, float maxR, float thick, int segs, float r, float g, float b, float aBase) {
        BufferBuilder buffer = null;

        for (Ring ring : rings) {
            float t = (float) Math.max(0.0, Math.min(1.0, (now - ring.startTime) / (double) life));
            float radius = maxR * t;
            float alpha = aBase * (1.0f - t);
            if (alpha <= 0.01f) continue;

            if (buffer == null)
                buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

            renderRingGeometry(buffer, matrix, ring, radius, thick, segs, r, g, b, alpha, cam);
        }

        if (buffer != null) BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void renderRingsWithGlow(Matrix4f matrix, Vec3d cam, long now, long life, float maxR, float thick, int segs, float r, float g, float b, float aBase) {
        float glowIntens = glowIntensity.getValueFloat();
        float glowRad = glowRadius.getValueFloat();

        for (int pass = 0; pass < 5; pass++) {
            BufferBuilder buffer = null;
            float passAlpha = 1.0f;
            float passThickness = thick;

            switch (pass) {
                case 0:
                    passAlpha = glowIntens * 0.15f;
                    passThickness = thick + glowRad * 4.0f;
                    RenderSystem.blendFunc(770, 1);
                    break;
                case 1:
                    passAlpha = glowIntens * 0.25f;
                    passThickness = thick + glowRad * 3.0f;
                    RenderSystem.blendFunc(770, 1);
                    break;
                case 2:
                    passAlpha = glowIntens * 0.4f;
                    passThickness = thick + glowRad * 2.0f;
                    RenderSystem.blendFunc(770, 1);
                    break;
                case 3:
                    passAlpha = glowIntens * 0.7f;
                    passThickness = thick + glowRad;
                    RenderSystem.blendFunc(770, 1);
                    break;
                case 4:
                    passThickness = thick;
                    RenderSystem.defaultBlendFunc();
                    break;
            }

            for (Ring ring : rings) {
                float t = (float) Math.max(0.0, Math.min(1.0, (now - ring.startTime) / (double) life));
                float radius = maxR * t;
                float alpha = aBase * (1.0f - t) * passAlpha;
                if (alpha <= 0.005f) continue;

                if (buffer == null)
                    buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

                renderRingGeometry(buffer, matrix, ring, radius, passThickness, segs, r, g, b, alpha, cam);
            }

            if (buffer != null) BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.defaultBlendFunc();
    }

    private void renderRingGeometry(BufferBuilder buffer, Matrix4f matrix, Ring ring, float radius, float thick, int segs, float r, float g, float b, float alpha, Vec3d cam) {
        for (int i = 0; i < segs; i++) {
            double a0 = (i / (double) segs) * Math.PI * 2.0;
            double a1 = ((i + 1) / (double) segs) * Math.PI * 2.0;

            double x0 = Math.cos(a0);
            double z0 = Math.sin(a0);
            double x1 = Math.cos(a1);
            double z1 = Math.sin(a1);

            Vec3d center = new Vec3d(ring.origin.x, ring.origin.y + 0.01, ring.origin.z);
            Vec3d p0i = center.add(x0 * radius, 0, z0 * radius);
            Vec3d p1i = center.add(x1 * radius, 0, z1 * radius);
            Vec3d p0o = center.add(x0 * (radius + thick), 0, z0 * (radius + thick));
            Vec3d p1o = center.add(x1 * (radius + thick), 0, z1 * (radius + thick));

            addTri(buffer, matrix, p0i, p1i, p1o, r, g, b, alpha, cam);
            addTri(buffer, matrix, p0i, p1o, p0o, r, g, b, alpha, cam);
        }
    }

    private record Ring(Vec3d origin, long startTime, boolean self) {
    }
}


