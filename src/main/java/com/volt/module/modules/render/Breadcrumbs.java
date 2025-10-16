package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.player.TickEvent;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public final class Breadcrumbs extends Module {

    private final NumberSetting spawnRate = new NumberSetting("Spawn Rate", 1, 20, 5, 1);
    private final NumberSetting lifetime = new NumberSetting("Lifetime", 1.0, 10.0, 3.0, 0.1);
    private final NumberSetting particleSize = new NumberSetting("Particle Size", 0.01, 0.2, 0.05, 0.005);
    private final NumberSetting spread = new NumberSetting("Spread", 0.0, 1.0, 0.3, 0.05);
    private final BooleanSetting physics = new BooleanSetting("Physics", true);
    private final NumberSetting gravity = new NumberSetting("Gravity", 0.0, 2.0, 0.5, 0.05);
    private final ColorSetting color = new ColorSetting("Color", new Color(255, 255, 255, 150));
    private final Deque<Particle> particles = new ArrayDeque<>();
    private Vec3d lastPos = null;
    private int tickCounter = 0;

    public Breadcrumbs() {
        super("Breadcrumbs", "Spawns particles when walking", -1, Category.RENDER);
        addSettings(spawnRate, lifetime, particleSize, spread, physics, gravity, color);
    }

    @EventHandler
    private void onTick(TickEvent e) {
        if (isNull()) return;

        Vec3d currentPos = mc.player.getPos();

        if (lastPos != null) {
            double distance = currentPos.distanceTo(lastPos);

            if (distance > 0.1) {
                tickCounter++;
                int rate = spawnRate.getValueInt();

                if (tickCounter >= (21 - rate)) {
                    spawnParticles(currentPos);
                    tickCounter = 0;
                }
            }
        }

        lastPos = currentPos;

        long now = System.currentTimeMillis();
        long maxLifetime = (long) (lifetime.getValue() * 1000);

        particles.removeIf(particle -> now - particle.spawn > maxLifetime);
    }

    private void spawnParticles(Vec3d pos) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int count = random.nextInt(1, 4);

        for (int i = 0; i < count; i++) {
            float spreadValue = spread.getValueFloat();
            double offsetX = (random.nextDouble() - 0.5) * spreadValue;
            double offsetZ = (random.nextDouble() - 0.5) * spreadValue;

            Vec3d particlePos = pos.add(offsetX, 0.1, offsetZ);
            Vec3d velocity = Vec3d.ZERO;

            if (physics.getValue()) {
                velocity = new Vec3d(
                        (random.nextDouble() - 0.5) * 0.1,
                        random.nextDouble() * 0.05,
                        (random.nextDouble() - 0.5) * 0.1
                );
            }

            float size = particleSize.getValueFloat() * (0.8f + random.nextFloat() * 0.4f);
            particles.add(new Particle(particlePos, velocity, size, System.currentTimeMillis()));
        }
    }

    @EventHandler
    private void onRender3D(EventRender3D e) {
        if (isNull() || particles.isEmpty()) return;

        MatrixStack matrices = e.getMatrixStack();
        Vec3d cam = mc.gameRenderer.getCamera().getPos();

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        long now = System.currentTimeMillis();
        long maxLifetime = (long) (lifetime.getValue() * 1000);
        float gravityValue = gravity.getValueFloat();
        Color c = color.getValue();
        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;
        float a = c.getAlpha() / 255.0f;

        float dt = mc.getRenderTickCounter().getTickDelta(true);

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            long age = now - p.spawn;
            if (age > maxLifetime) continue;

            if (physics.getValue()) {
                p.vel = p.vel.add(0, -gravityValue * dt * 0.01, 0);
                p.pos = p.pos.add(p.vel.multiply(dt));
            }

            float ageRatio = age / (float) maxLifetime;
            float alpha = a * (1.0f - ageRatio);
            float currentSize = p.size * (1.0f - ageRatio * 0.3f);

            matrices.push();
            matrices.translate(p.pos.x - cam.x, p.pos.y - cam.y, p.pos.z - cam.z);

            float yaw = mc.gameRenderer.getCamera().getYaw();
            float pitch = mc.gameRenderer.getCamera().getPitch();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(p.rotation + p.rotationSpeed * ageRatio * 100));

            renderParticle(matrices, currentSize, r, g, b, alpha);
            matrices.pop();
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private void renderParticle(MatrixStack matrices, float size, float r, float g, float b, float alpha) {
        Matrix4f m = matrices.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        float edgeA = alpha * 0.05f;
        int segs = 24;
        for (int i = 0; i < segs; i++) {
            double a0 = (i / (double) segs) * Math.PI * 2.0;
            double a1 = ((i + 1) / (double) segs) * Math.PI * 2.0;
            float x0 = (float) (Math.cos(a0) * size);
            float y0 = (float) (Math.sin(a0) * size);
            float x1 = (float) (Math.cos(a1) * size);
            float y1 = (float) (Math.sin(a1) * size);
            buf.vertex(m, 0, 0, 0).color(r, g, b, alpha);
            buf.vertex(m, x0, y0, 0).color(r, g, b, edgeA);
            buf.vertex(m, x1, y1, 0).color(r, g, b, edgeA);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    private static final class Particle {
        Vec3d pos;
        Vec3d vel;
        float size;
        long spawn;
        float rotation;
        float rotationSpeed;

        Particle(Vec3d pos, Vec3d vel, float size, long spawn) {
            this.pos = pos;
            this.vel = vel;
            this.size = size;
            this.spawn = spawn;
            this.rotation = ThreadLocalRandom.current().nextFloat() * 360f;
            this.rotationSpeed = (ThreadLocalRandom.current().nextFloat() - 0.5f) * 2f;
        }
    }
}