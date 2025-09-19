package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.player.EventAttack;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class HitOrbs extends Module {

    private static final class TrailPoint { final Vec3d pos; final long time; TrailPoint(Vec3d pos, long time) { this.pos = pos; this.time = time; } }
    private static final class OrbTrail { final Deque<TrailPoint> points = new ArrayDeque<>(); final double thetaOffset; final double phiOffset; OrbTrail(double to, double po) { this.thetaOffset = to; this.phiOffset = po; } }
    private static final class Effect { final int entityId; final long start; final List<OrbTrail> orbs; Effect(int entityId, long start, List<OrbTrail> orbs) { this.entityId = entityId; this.start = start; this.orbs = orbs; } }

    private final NumberSetting duration = new NumberSetting("Duration", 0.1, 3.0, 0.9, 0.05);
    private final NumberSetting count = new NumberSetting("Orbs", 1, 6, 3, 1);
    private final NumberSetting orbitRadius = new NumberSetting("Orbit Radius", 0.2, 2.5, 0.9, 0.05);
    private final NumberSetting orbSize = new NumberSetting("Orb Size", 0.05, 0.8, 0.25, 0.01);
    private final NumberSetting heightOffset = new NumberSetting("Height Offset", 0.0, 2.0, 0.9, 0.05);
    private final NumberSetting angularSpeed = new NumberSetting("Angular Speed", 90.0, 720.0, 240.0, 5.0);
    private final NumberSetting segments = new NumberSetting("Segments", 8, 64, 28, 1);
    private final NumberSetting verticalSpeed = new NumberSetting("Vertical Speed", 30.0, 360.0, 120.0, 5.0);
    private final NumberSetting trailSeconds = new NumberSetting("Trail Seconds", 0.0, 2.0, 0.6, 0.05);
    private final NumberSetting trailWidth = new NumberSetting("Trail Width", 0.01, 0.3, 0.06, 0.005);
    private final ColorSetting color = new ColorSetting("Color", new Color(120, 240, 255, 220));

    private final List<Effect> effects = new ArrayList<>();

    public HitOrbs() {
        super("Hit Orbs", "Spawns orbs orbiting a target on hit", -1, Category.RENDER);
        addSettings(duration, count, orbitRadius, orbSize, heightOffset, angularSpeed, segments, verticalSpeed, trailSeconds, trailWidth, color);
    }

    @EventHandler
    private void onAttack(EventAttack e) {
        Entity t = e.getTarget();
        if (t instanceof LivingEntity) {
            int n = Math.max(1, count.getValueInt());
            List<OrbTrail> trails = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                double theta0 = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2.0);
                double phi0 = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2.0);
                trails.add(new OrbTrail(theta0, phi0));
            }
            effects.add(new Effect(t.getId(), System.currentTimeMillis(), trails));
        }
    }

    @EventHandler
    private void onRender3D(EventRender3D e) {
        if (isNull() || effects.isEmpty()) return;

        MatrixStack matrices = e.getMatrixStack();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        long now = System.currentTimeMillis();
        float size = orbSize.getValueFloat();
        float radius = orbitRadius.getValueFloat();
        float yOff = heightOffset.getValueFloat();
        float speedDeg = angularSpeed.getValueFloat();
        float vSpeedDeg = verticalSpeed.getValueFloat();
        int segs = Math.max(8, segments.getValueInt());
        Color c = color.getValue();
        long trailTTL = (long) (trailSeconds.getValue() * 1000.0);
        float ribbonWidth = trailWidth.getValueFloat();

        Iterator<Effect> it = effects.iterator();
        while (it.hasNext()) {
            Effect fx = it.next();
            Entity ent = mc.world.getEntityById(fx.entityId);
            if (!(ent instanceof LivingEntity) || !ent.isAlive()) { it.remove(); continue; }

            double tNorm = (now - fx.start) / (duration.getValue() * 1000.0);
            if (tNorm >= 1.0) { it.remove(); continue; }

            double baseAngle = Math.toRadians((now - fx.start) * (speedDeg / 1000.0));
            float pt = mc.getRenderTickCounter().getTickDelta(true);
            double cx = ent.prevX + (ent.getX() - ent.prevX) * pt;
            double cy = ent.prevY + (ent.getY() - ent.prevY) * pt;
            double cz = ent.prevZ + (ent.getZ() - ent.prevZ) * pt;
            Vec3d center = new Vec3d(cx, cy, cz).add(0, ent.getHeight() * 0.5 + yOff, 0);

            int orbCount = fx.orbs.size();
            for (int i = 0; i < orbCount; i++) {
                OrbTrail ot = fx.orbs.get(i);
                double theta = baseAngle + ot.thetaOffset;
                double phi = (Math.toRadians(vSpeedDeg) * (now - fx.start) / 1000.0) + ot.phiOffset;
                double sinPhi = (Math.sin(phi) * 0.5) + 0.5;
                double cxr = radius * sinPhi;
                double oy = radius * (1.0 - sinPhi) - radius * 0.5;
                double ox = Math.cos(theta) * cxr;
                double oz = Math.sin(theta) * cxr;
                Vec3d pos = center.add(ox, oy, oz);

                matrices.push();
                matrices.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
                float yaw = mc.gameRenderer.getCamera().getYaw();
                float pitch = mc.gameRenderer.getCamera().getPitch();
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));

                RenderSystem.setShader(GameRenderer::getPositionColorProgram);
                drawOrbBillboard(matrices, size, segs, c);
                matrices.pop();

                if (trailTTL > 1) {
                    Deque<TrailPoint> pts = ot.points;
                    pts.addLast(new TrailPoint(pos, now));
                    while (!pts.isEmpty() && now - pts.peekFirst().time > trailTTL) pts.removeFirst();

                    if (pts.size() >= 3) {
                        BufferBuilder rb = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
                        boolean emitted = addRibbon(rb, pts, ribbonWidth, c, cam, now, trailTTL);
                        if (emitted) BufferRenderer.drawWithGlobalProgram(rb.end());
                    }
                }
            }
        }
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private void drawOrbBillboard(MatrixStack matrices, float radius, int segs, Color color) {
        Matrix4f m = matrices.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float aCenter = color.getAlpha() / 255.0f;
        float aEdge = 0.05f;

        for (int i = 0; i < segs; i++) {
            double a0 = (i / (double) segs) * Math.PI * 2.0;
            double a1 = ((i + 1) / (double) segs) * Math.PI * 2.0;
            float x0 = (float) (Math.cos(a0) * radius);
            float y0 = (float) (Math.sin(a0) * radius);
            float x1 = (float) (Math.cos(a1) * radius);
            float y1 = (float) (Math.sin(a1) * radius);

            buf.vertex(m, 0, 0, 0).color(r, g, b, aCenter);
            buf.vertex(m, x0, y0, 0).color(r, g, b, aEdge);
            buf.vertex(m, x1, y1, 0).color(r, g, b, aEdge);
        }

        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private static boolean addRibbon(BufferBuilder buffer, Deque<TrailPoint> pts, float width, Color color, Vec3d cam, long now, long ttl) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float aBase = color.getAlpha() / 255.0f;

        TrailPoint prev = null;
        Vec3d prevLeft = null;
        Vec3d prevRight = null;
        boolean emitted = false;

        for (TrailPoint tp : pts) {
            if (prev != null) {
                Vec3d seg = tp.pos.subtract(prev.pos);
                if (seg.lengthSquared() < 1.0E-6) { prev = tp; continue; }
                Vec3d cameraDir = tp.pos.subtract(cam).normalize();
                Vec3d perp = seg.crossProduct(cameraDir).normalize().multiply(width);

                Vec3d left = tp.pos.add(perp);
                Vec3d right = tp.pos.subtract(perp);

                float a0 = aBase * (float) Math.max(0.0, Math.min(1.0, 1.0 - (now - prev.time) / (double) ttl));
                float a1 = aBase * (float) Math.max(0.0, Math.min(1.0, 1.0 - (now - tp.time) / (double) ttl));

                if (prevLeft != null && prevRight != null) {
                    buffer.vertex((float) (prevLeft.x - cam.x), (float) (prevLeft.y - cam.y), (float) (prevLeft.z - cam.z)).color(r, g, b, a0);
                    buffer.vertex((float) (prevRight.x - cam.x), (float) (prevRight.y - cam.y), (float) (prevRight.z - cam.z)).color(r, g, b, a0);
                    buffer.vertex((float) (right.x - cam.x), (float) (right.y - cam.y), (float) (right.z - cam.z)).color(r, g, b, a1);

                    buffer.vertex((float) (prevLeft.x - cam.x), (float) (prevLeft.y - cam.y), (float) (prevLeft.z - cam.z)).color(r, g, b, a0);
                    buffer.vertex((float) (right.x - cam.x), (float) (right.y - cam.y), (float) (right.z - cam.z)).color(r, g, b, a1);
                    buffer.vertex((float) (left.x - cam.x), (float) (left.y - cam.y), (float) (left.z - cam.z)).color(r, g, b, a1);
                    emitted = true;
                }

                prevLeft = left;
                prevRight = right;
            }
            prev = tp;
        }
        return emitted;
    }
}


