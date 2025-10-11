package com.volt.module.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.volt.event.impl.player.AttackEvent;
import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.ColorSetting;
import com.volt.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

public final class HitParticles extends Module {

    private final NumberSetting perHit = new NumberSetting("Particles/Hit", 5, 200, 40, 1);
    private final NumberSetting maxLifetime = new NumberSetting("Lifetime (s)", 0.2, 4.0, 1.6, 0.1);
    private final NumberSetting gravity = new NumberSetting("Gravity", 0.0, 2.0, 0.7, 0.05);
    private final NumberSetting restitution = new NumberSetting("Bounce", 0.0, 1.0, 0.45, 0.05);
    private final NumberSetting friction = new NumberSetting("Friction", 0.0, 1.0, 0.12, 0.02);
    private final NumberSetting baseSpeed = new NumberSetting("Base Speed", 0.1, 2.5, 1.0, 0.05);
    private final NumberSetting sizeMin = new NumberSetting("Size Min", 0.02, 0.4, 0.06, 0.01);
    private final NumberSetting sizeMax = new NumberSetting("Size Max", 0.02, 0.8, 0.16, 0.01);
    private final ColorSetting color = new ColorSetting("Color", new Color(220, 240, 255, 230));
    private final Deque<Particle> particles = new ArrayDeque<>();

    public HitParticles() {
        super("Hit Particles", "Spawns particles on hit", -1, Category.RENDER);
        addSettings(perHit, maxLifetime, gravity, restitution, friction, baseSpeed, sizeMin, sizeMax, color);
    }

    private static void drawDisc(MatrixStack matrices, float radius, float r, float g, float b, float a) {
        Matrix4f m = matrices.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        float edgeA = a * 0.05f;
        int segs = 24;
        for (int i = 0; i < segs; i++) {
            double a0 = (i / (double) segs) * Math.PI * 2.0;
            double a1 = ((i + 1) / (double) segs) * Math.PI * 2.0;
            float x0 = (float) (Math.cos(a0) * radius);
            float y0 = (float) (Math.sin(a0) * radius);
            float x1 = (float) (Math.cos(a1) * radius);
            float y1 = (float) (Math.sin(a1) * radius);
            buf.vertex(m, 0, 0, 0).color(r, g, b, a);
            buf.vertex(m, x0, y0, 0).color(r, g, b, edgeA);
            buf.vertex(m, x1, y1, 0).color(r, g, b, edgeA);
        }
        BufferRenderer.drawWithGlobalProgram(buf.end());
    }

    @EventHandler
    private void onAttack(AttackEvent e) {
        Entity t = e.getTarget();
        if (!(t instanceof LivingEntity)) return;

        long now = System.currentTimeMillis();
        int count = perHit.getValueInt();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        Vec3d origin = t.getPos().add(0, t.getHeight() * 0.5, 0);
        float base = baseSpeed.getValueFloat();

        for (int i = 0; i < count; i++) {
            double theta = rnd.nextDouble(0, Math.PI * 2.0);
            double phi = Math.acos(rnd.nextDouble(-1.0, 1.0));
            double speed = base * (0.6 + rnd.nextDouble() * 0.8);
            Vec3d vel = new Vec3d(
                    Math.cos(theta) * Math.sin(phi) * speed,
                    Math.cos(phi) * speed,
                    Math.sin(theta) * Math.sin(phi) * speed);
            float size = (float) (sizeMin.getValue() + rnd.nextDouble() * Math.max(0.01, sizeMax.getValue() - sizeMin.getValue()));
            particles.addLast(new Particle(origin, vel, size, now));
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
        long ttl = (long) (maxLifetime.getValue() * 1000.0);
        float g = gravity.getValueFloat();
        float bounce = restitution.getValueFloat();
        float fric = friction.getValueFloat();
        Color c = color.getValue();
        float cr = c.getRed() / 255.0f, cg = c.getGreen() / 255.0f, cb = c.getBlue() / 255.0f, ca = c.getAlpha() / 255.0f;

        float dt = mc.getRenderTickCounter().getTickDelta(true);

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            if (now - p.spawn > ttl) {
                it.remove();
                continue;
            }

            p.vel = p.vel.add(0, -g * dt, 0);

            Vec3d next = p.pos.add(p.vel.multiply(dt));
            BlockPos below = BlockPos.ofFloored(next.x, next.y - p.size, next.z);
            boolean hitsGround = !mc.world.getBlockState(below).isAir();

            if (hitsGround && p.vel.y < 0) {
                next = new Vec3d(next.x, below.getY() + 1 + p.size, next.z);
                p.vel = new Vec3d(p.vel.x * (1.0 - fric), -p.vel.y * bounce, p.vel.z * (1.0 - fric));
                if (Math.abs(p.vel.y) < 0.02) p.vel = new Vec3d(p.vel.x * 0.9, 0, p.vel.z * 0.9);
            }

            p.pos = next;

            matrices.push();
            matrices.translate(p.pos.x - cam.x, p.pos.y - cam.y, p.pos.z - cam.z);
            float yaw = mc.gameRenderer.getCamera().getYaw();
            float pitch = mc.gameRenderer.getCamera().getPitch();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));

            drawDisc(matrices, p.size, cr, cg, cb, ca * (1.0f - (now - p.spawn) / (float) ttl));
            matrices.pop();
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static final class Particle {
        Vec3d pos;
        Vec3d vel;
        float size;
        long spawn;

        Particle(Vec3d pos, Vec3d vel, float size, long spawn) {
            this.pos = pos;
            this.vel = vel;
            this.size = size;
            this.spawn = spawn;
        }
    }
}


