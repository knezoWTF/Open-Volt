package com.volt.module.modules.combat;

import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.modules.misc.Teams;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ModeSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.friend.FriendManager;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AimAssist extends Module {
    private static final ModeSetting mode = new ModeSetting("Aim Mode", "Distance", "Distance", "Health");
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 10.0, 5.0, 0.1);
    private final NumberSetting fov = new NumberSetting("FOV", 10.0, 180.0, 90.0, 1.0);
    private final NumberSetting range = new NumberSetting("Range", 1.0, 10.0, 4.5, 0.1);
    private final NumberSetting pitchSpeed = new NumberSetting("Pitch Speed", 0.1, 5.0, 2.0, 0.1);
    private final NumberSetting yawSpeed = new NumberSetting("Yaw Speed", 0.1, 5.0, 2.0, 0.1);
    private final NumberSetting smoothing = new NumberSetting("Smoothing", 1.0, 20.0, 10.0, 0.5);
    private final NumberSetting noiseSpeed = new NumberSetting("Noise Speed", 0.5, 5.0, 1.5, 0.5);
    private final BooleanSetting targetPlayers = new BooleanSetting("Target Players", true);
    private final BooleanSetting targetMobs = new BooleanSetting("Target Mobs", false);
    private final BooleanSetting weaponsOnly = new BooleanSetting("Weapons Only", false);
    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", false);
    private final BooleanSetting disableOnTarget = new BooleanSetting("Disable on target", false);
    private final BooleanSetting workOnlyWhileRMBPressed = new BooleanSetting("Work only when RMB is pressed", false);

    private Entity currentTarget = null;
    private long lastUpdateTime = 0;
    private float noiseTime = 0f;

    public AimAssist() {
        super("Aim Assist", "Helps you with aiming", Category.COMBAT);
        addSettings(
            mode, speed, fov, range, pitchSpeed, yawSpeed, smoothing, noiseSpeed,
            targetPlayers, targetMobs, weaponsOnly, throughWalls, disableOnTarget, workOnlyWhileRMBPressed
        );
    }

    @EventHandler
    private void onRender3D(EventRender3D event) {
        if (isNull()) return;

        if (weaponsOnly.getValue() && !isHoldingWeapon()) return;
        if (mc.currentScreen != null) return;

        HitResult hit = mc.crosshairTarget;

        if (disableOnTarget.getValue() && hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (isValidTarget(entity) && entity != currentTarget) return;
        }

        if (workOnlyWhileRMBPressed.getValue() &&
            GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) != GLFW.GLFW_PRESS
        ) return;

        currentTarget = findBestTarget();

        if (currentTarget != null) {
            if (!throughWalls.getValue() && !mc.player.canSee(currentTarget)) return;

            Vec3d chestPos = getChestPosition(currentTarget);
            float[] rotation = calculateRotation(chestPos);
            applySmoothAiming(rotation[0], rotation[1]);
        }
    }

    private Entity findBestTarget() {
        if (isNull()) return null;

        for (Entity entity : mc.world.getEntities()) {
            if (!isValidTarget(entity)) continue;
            double distance = mc.player.distanceTo(entity);
            if (distance <= range.getValue()) {
                Vec3d chestPos = getChestPosition(entity);
                float[] rotation = calculateRotation(chestPos);
                double fovDistance = getFOVDistance(rotation[0], rotation[1]);
                if (fovDistance <= fov.getValue() / 2.0) return entity;
                break;
            }
        }

        Entity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (!isValidTarget(entity)) continue;
            double distance = mc.player.distanceTo(entity);
            if (distance > range.getValue()) continue;

            Vec3d chestPos = getChestPosition(entity);
            float[] rotation = calculateRotation(chestPos);
            double fovDistance = getFOVDistance(rotation[0], rotation[1]);

            if (fovDistance <= fov.getValue() / 2.0) {
                double score;
                if (mode.getMode().equals("Health") && entity instanceof LivingEntity livingEntity) {
                    float health = livingEntity.getHealth() + livingEntity.getAbsorptionAmount();
                    score = health + (fovDistance * 2.0);
                } else {
                    score = distance + (fovDistance * 2.0);
                }
                if (score < bestScore) {
                    bestScore = score;
                    bestTarget = entity;
                }
            }
        }

        return bestTarget;
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == null || entity == mc.player || !(entity instanceof LivingEntity livingEntity)) return false;
        if (!livingEntity.isAlive() || livingEntity.isDead()) return false;
        if (entity instanceof PlayerEntity player && FriendManager.isFriend(player.getUuid())) return false;
        if (Teams.isTeammate(entity)) return false;
        return entity instanceof PlayerEntity ? targetPlayers.getValue() : targetMobs.getValue();
    }

    private float humanNoise(float t) {
        return (float)(
            (Math.random() * 0.01) +
            (Math.sin(t * 1.3) * 0.005) +
            (Math.sin(t * 0.7 + 2.1) * 0.003)
        );
    }

    private Vec3d getChestPosition(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY() + entity.getHeight() * 0.6, entity.getZ());
    }

    private float[] calculateRotation(Vec3d target) {
        Vec3d diff = target.subtract(mc.player.getEyePos());
        double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distance));
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -89.0f, 89.0f)};
    }

    private double getFOVDistance(float targetYaw, float targetPitch) {
        float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDiff = targetPitch - mc.player.getPitch();
        return Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
    }

    private float easing(float t) {
        return (float)(1.0 / (1.0 + Math.exp(-5.0 * (t - 0.5))));
    }

    private void applySmoothAiming(float targetYaw, float targetPitch) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000.0f, 0.1f);
        lastUpdateTime = currentTime;

        float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDiff = targetPitch - mc.player.getPitch();
        float angularDistance = (float) Math.hypot(yawDiff, pitchDiff);
        float normalizedDist = MathHelper.clamp(angularDistance / 90f, 0.0f, 1.0f);

        float easingFactor = easing(normalizedDist);
        float minStepScale = 0.01f;
        float stepScale = easingFactor * (float) (speed.getValue() * deltaTime / smoothing.getValue());
        stepScale = Math.max(stepScale, minStepScale);

        float yawStep = MathHelper.clamp(yawDiff * stepScale * (float) yawSpeed.getValue(), -10.0f, 10.0f);
        float pitchStep = MathHelper.clamp(pitchDiff * stepScale * (float) pitchSpeed.getValue(), -10.0f, 10.0f);

        noiseTime += deltaTime * noiseSpeed.getValueFloat();
        yawStep += humanNoise(noiseTime);
        pitchStep += humanNoise(noiseTime + 50f);

        if (Math.abs(yawDiff) > 0.1f || Math.abs(pitchDiff) > 0.1f) {
            float newYaw = mc.player.getYaw() + yawStep;
            float newPitch = mc.player.getPitch() + pitchStep;

            newYaw = applyGcdSnap(mc.player.getYaw(), newYaw);
            newPitch = applyGcdSnap(mc.player.getPitch(), newPitch);

            mc.player.setYaw(newYaw);
            mc.player.setPitch(MathHelper.clamp(newPitch, -89.0f, 89.0f));
        }
    }

    private float applyGcdSnap(float current, float target) {
        double sens = mc.options.getMouseSensitivity().getValue();
        float f = (float) (sens * 0.6f + 0.2f);
        float gcd = f * f * f * 8.0f;
        float diff = target - current;
        return current + Math.round(diff / gcd) * gcd;
    }

    private boolean isHoldingWeapon() {
        if (mc.player == null) return false;
        if (mc.player.getMainHandStack().isEmpty()) return false;
        Item heldItem = mc.player.getMainHandStack().getItem();
        return heldItem instanceof SwordItem || heldItem instanceof AxeItem;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        currentTarget = null;
    }
}