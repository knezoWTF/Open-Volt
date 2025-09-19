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

    private static final class SmokeParticle { 
        final Vec3d pos; 
        final Vec3d velocity; 
        final long spawnTime; 
        final float initialSize; 
        final float maxSize; 
        final double rotationSpeed;
        final double rotation;
        
        SmokeParticle(Vec3d pos, Vec3d velocity, long spawnTime, float initialSize, float maxSize, double rotationSpeed) { 
            this.pos = pos; 
            this.velocity = velocity; 
            this.spawnTime = spawnTime; 
            this.initialSize = initialSize; 
            this.maxSize = maxSize; 
            this.rotationSpeed = rotationSpeed;
            this.rotation = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2.0);
        } 
    }
    
    private static final class SmokeTrail { 
        final Deque<SmokeParticle> particles = new ArrayDeque<>(); 
        final double thetaOffset; 
        final double phiOffset; 
        
        SmokeTrail(double to, double po) { 
            this.thetaOffset = to; 
            this.phiOffset = po; 
        } 
    }
    
    private static final class Effect { 
        final int entityId; 
        final long start; 
        final List<SmokeTrail> orbs; 
        
        Effect(int entityId, long start, List<SmokeTrail> orbs) { 
            this.entityId = entityId; 
            this.start = start; 
            this.orbs = orbs; 
        } 
    }

    private final NumberSetting duration = new NumberSetting("Duration", 0.1, 3.0, 0.9, 0.05);
    private final NumberSetting count = new NumberSetting("Orbs", 1, 6, 3, 1);
    private final NumberSetting orbitRadius = new NumberSetting("Orbit Radius", 0.2, 2.5, 0.9, 0.05);
    private final NumberSetting orbSize = new NumberSetting("Orb Size", 0.05, 0.8, 0.25, 0.01);
    private final NumberSetting smokeIntensity = new NumberSetting("Smoke Intensity", 0.0, 2.0, 1.0, 0.1);
    private final ColorSetting color = new ColorSetting("Color", new Color(120, 240, 255, 220));

    private final List<Effect> effects = new ArrayList<>();

    public HitOrbs() {
        super("Hit Orbs", "Spawns orbs orbiting a target on hit", -1, Category.RENDER);
        addSettings(duration, count, orbitRadius, orbSize, smokeIntensity, color);
    }

    @EventHandler
    private void onAttack(EventAttack e) {
        Entity t = e.getTarget();
        if (t instanceof LivingEntity) {
            int n = Math.max(1, count.getValueInt());
            List<SmokeTrail> trails = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                double theta0 = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2.0);
                double phi0 = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2.0);
                trails.add(new SmokeTrail(theta0, phi0));
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
        float intensity = smokeIntensity.getValueFloat();
        float yOff = 0.9f;
        float speedDeg = 240.0f;
        float vSpeedDeg = 120.0f;
        int segs = 28;
        Color c = color.getValue();
        long smokeTTL = (long) (intensity * 1200.0);
        int particlesPerFrame = Math.max(1, (int)(intensity * 6));
        float expansion = 1.0f + intensity;
        float drift = intensity * 0.3f;

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
                SmokeTrail st = fx.orbs.get(i);
                double theta = baseAngle + st.thetaOffset;
                double phi = (Math.toRadians(vSpeedDeg) * (now - fx.start) / 1000.0) + st.phiOffset;
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

                if (smokeTTL > 50) {
                    Deque<SmokeParticle> particles = st.particles;
                    
                    for (int p = 0; p < particlesPerFrame; p++) {
                        Vec3d smokeVel = new Vec3d(
                            (ThreadLocalRandom.current().nextDouble() - 0.5) * drift * 0.15,
                            (ThreadLocalRandom.current().nextDouble() - 0.3) * drift * 0.12,
                            (ThreadLocalRandom.current().nextDouble() - 0.5) * drift * 0.15
                        );
                        
                        float initialSize = size * 0.3f;
                        float maxSize = size * expansion;
                        double rotSpeed = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0;
                        
                        particles.addLast(new SmokeParticle(pos, smokeVel, now, initialSize, maxSize, rotSpeed));
                    }
                    
                    while (!particles.isEmpty() && now - particles.peekFirst().spawnTime > smokeTTL) {
                        particles.removeFirst();
                    }

                    if (!particles.isEmpty()) {
                        renderSmokeParticles(particles, cam, now, smokeTTL, c);
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

    private void renderSmokeParticles(Deque<SmokeParticle> particles, Vec3d cam, long now, long ttl, Color color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float baseAlpha = color.getAlpha() / 255.0f * 0.6f;

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        
        for (SmokeParticle particle : particles) {
            long age = now - particle.spawnTime;
            double ageNorm = (double) age / ttl;
            
            if (ageNorm >= 1.0) continue;
            
            Vec3d gravity = new Vec3d(0, -0.001 * ageNorm, 0);
            Vec3d wind = new Vec3d(
                Math.sin(now * 0.001 + particle.rotation) * 0.002,
                0,
                Math.cos(now * 0.001 + particle.rotation) * 0.002
            );
            Vec3d totalVel = particle.velocity.add(gravity).add(wind);
            Vec3d currentPos = particle.pos.add(totalVel.multiply(age / 1000.0));
            
            float sizeProgress = (float) Math.min(1.0, ageNorm * 2.0);
            float currentSize = particle.initialSize + (particle.maxSize - particle.initialSize) * sizeProgress;
            
            float alpha = baseAlpha * (float) (1.0 - Math.pow(ageNorm, 1.5));
            
            double currentRotation = particle.rotation + particle.rotationSpeed * (age / 1000.0);
            
            renderSmokeParticle(buf, currentPos, currentSize, alpha, currentRotation, r, g, b, cam);
        }
        
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }
    
    private void renderSmokeParticle(BufferBuilder buf, Vec3d pos, float size, float alpha, double rotation, float r, float g, float b, Vec3d cam) {
        float x = (float) (pos.x - cam.x);
        float y = (float) (pos.y - cam.y);
        float z = (float) (pos.z - cam.z);
        
        float cos = (float) Math.cos(rotation);
        float sin = (float) Math.sin(rotation);
        
        float x1 = (-size * cos - -size * sin);
        float y1 = (-size * sin + -size * cos);
        float x2 = (size * cos - -size * sin);
        float y2 = (size * sin + -size * cos);
        float x3 = (size * cos - size * sin);
        float y3 = (size * sin + size * cos);
        float x4 = (-size * cos - size * sin);
        float y4 = (-size * sin + size * cos);
        
        float centerAlpha = alpha;
        float edgeAlpha = alpha * 0.1f;
        
        buf.vertex(x, y, z).color(r, g, b, centerAlpha);
        buf.vertex(x + x1, y + y1, z).color(r, g, b, edgeAlpha);
        buf.vertex(x + x2, y + y2, z).color(r, g, b, edgeAlpha);
        
        buf.vertex(x, y, z).color(r, g, b, centerAlpha);
        buf.vertex(x + x2, y + y2, z).color(r, g, b, edgeAlpha);
        buf.vertex(x + x3, y + y3, z).color(r, g, b, edgeAlpha);
        
        buf.vertex(x, y, z).color(r, g, b, centerAlpha);
        buf.vertex(x + x3, y + y3, z).color(r, g, b, edgeAlpha);
        buf.vertex(x + x4, y + y4, z).color(r, g, b, edgeAlpha);
        
        buf.vertex(x, y, z).color(r, g, b, centerAlpha);
        buf.vertex(x + x4, y + y4, z).color(r, g, b, edgeAlpha);
        buf.vertex(x + x1, y + y1, z).color(r, g, b, edgeAlpha);
    }
}


