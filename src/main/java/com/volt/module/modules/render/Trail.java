package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.player.TickEvent;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.ModeSetting;
import com.volt.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Trail extends Module {

    private record TrailPoint(Vec3d pos, long time) {
    }

    private final ModeSetting targets = new ModeSetting("Targets", "Players", "Players", "Self");
    private final NumberSetting width = new NumberSetting("Width", 0.02, 0.4, 0.12, 0.01);
    private final NumberSetting seconds = new NumberSetting("Seconds", 0.2, 5.0, 2.0, 0.1);
    private final NumberSetting maxPoints = new NumberSetting("Max Points", 16, 256, 96, 1);
    private final ColorSetting color = new ColorSetting("Color", new Color(0, 200, 255, 160));

    private final Map<UUID, Deque<TrailPoint>> trails = new HashMap<>();

    public Trail() {
        super("Trail", "Trail behind players", -1, Category.RENDER);
        addSettings(targets, width, seconds, maxPoints, color);
    }

    @EventHandler
    private void onTick(TickEvent e) {
        if (isNull()) return;
        long now = System.currentTimeMillis();
        long ttl = (long) (seconds.getValue() * 1000.0);

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (!shouldTrack(p)) continue;
            Deque<TrailPoint> deque = trails.computeIfAbsent(p.getUuid(), k -> new ArrayDeque<>());
            Vec3d pos = p.getPos().add(0, p.getHeight() * 0.5, 0);
            if (deque.isEmpty() || deque.peekLast().pos.squaredDistanceTo(pos) > 0.0004) {
                deque.addLast(new TrailPoint(pos, now));
                while (deque.size() > maxPoints.getValueInt()) deque.removeFirst();
            }
        }

        trails.values().forEach(d -> {
            while (!d.isEmpty() && now - d.peekFirst().time > ttl) d.removeFirst();
        });
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

        BufferBuilder buffer = null;

        long now = System.currentTimeMillis();
        long ttl = (long) (seconds.getValue() * 1000.0);
        Color base = color.getValue();
        float br = base.getRed() / 255.0f;
        float bg = base.getGreen() / 255.0f;
        float bb = base.getBlue() / 255.0f;
        int baseA = base.getAlpha();

        for (Map.Entry<UUID, Deque<TrailPoint>> entry : trails.entrySet()) {
            Deque<TrailPoint> deque = entry.getValue();
            if (deque.size() < 2) continue;

            TrailPoint prev = null;
            for (TrailPoint point : deque) {
                if (prev != null) {
                    Vec3d p1 = prev.pos;
                    Vec3d p2 = point.pos;

                    Vec3d mid = p1.add(p2).multiply(0.5);
                    Vec3d cameraDir = mid.subtract(cam).normalize();

                    Vec3d seg = p2.subtract(p1);
                    if (seg.lengthSquared() < 1.0E-6) { prev = point; continue; }
                    Vec3d perp = seg.crossProduct(cameraDir);
                    if (perp.lengthSquared() < 1.0E-6) perp = new Vec3d(0, 1, 0).crossProduct(seg);
                    perp = perp.normalize().multiply(width.getValue());

                    float a1 = (float) Math.max(0.0, Math.min(1.0, 1.0 - (now - prev.time) / (double) ttl));
                    float a2 = (float) Math.max(0.0, Math.min(1.0, 1.0 - (now - point.time) / (double) ttl));
                    float ar1 = (baseA / 255.0f) * a1;
                    float ar2 = (baseA / 255.0f) * a2;

                    Vec3d p1l = p1.add(perp);
                    Vec3d p1r = p1.subtract(perp);
                    Vec3d p2l = p2.add(perp);
                    Vec3d p2r = p2.subtract(perp);

                    if (buffer == null) {
                        buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
                    }
                    addQuad(buffer, matrix, p1l, p1r, p2r, p2l, br, bg, bb, ar1, ar2, cam);
                }
                prev = point;
            }
        }

        if (buffer != null) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.enableDepthTest();
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f matrix, Vec3d v0, Vec3d v1, Vec3d v2, Vec3d v3,
                                float r, float g, float b, float aStart, float aEnd, Vec3d cam) {

        buffer.vertex(matrix, (float) (v0.x - cam.x), (float) (v0.y - cam.y), (float) (v0.z - cam.z)).color(r, g, b, aStart);
        buffer.vertex(matrix, (float) (v1.x - cam.x), (float) (v1.y - cam.y), (float) (v1.z - cam.z)).color(r, g, b, aStart);
        buffer.vertex(matrix, (float) (v2.x - cam.x), (float) (v2.y - cam.y), (float) (v2.z - cam.z)).color(r, g, b, aEnd);

        buffer.vertex(matrix, (float) (v0.x - cam.x), (float) (v0.y - cam.y), (float) (v0.z - cam.z)).color(r, g, b, aStart);
        buffer.vertex(matrix, (float) (v2.x - cam.x), (float) (v2.y - cam.y), (float) (v2.z - cam.z)).color(r, g, b, aEnd);
        buffer.vertex(matrix, (float) (v3.x - cam.x), (float) (v3.y - cam.y), (float) (v3.z - cam.z)).color(r, g, b, aEnd);
    }

    private boolean shouldTrack(PlayerEntity p) {
        if (targets.isMode("Self")) return p == mc.player;
        return true;
    }
}


