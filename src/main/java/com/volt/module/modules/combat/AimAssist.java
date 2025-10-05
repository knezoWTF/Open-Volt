package com.volt.module.modules.combat;

import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.modules.misc.Teams;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.MathUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AimAssist extends Module {


    private final NumberSetting maxYawSpeed = new NumberSetting("Max Yaw Speed", 0.1, 5.0, 2.0, 0.1);
    private final NumberSetting minYawSpeed = new NumberSetting("Min Yaw Speed", 0.1, 5.0, 2.0, 0.1);

    private final NumberSetting minPitchSpeed = new NumberSetting("Min Pitch Speed", 0.1, 5.0, 2.0, 0.1);
    private final NumberSetting maxPitchSpeed = new NumberSetting("Max Pitch Speed", 0.1, 5.0, 2.0, 0.1);

    private final NumberSetting fov = new NumberSetting("FOV", 10.0, 180.0, 90.0, 1.0);
    private final NumberSetting range = new NumberSetting("Range", 1.0, 10.0, 4.5, 0.1);
    private final NumberSetting smoothing = new NumberSetting("Smoothing", 1.0, 20.0, 10.0, 0.5);

    private final BooleanSetting targetPlayers = new BooleanSetting("Target Players", true);
    private final BooleanSetting targetMobs = new BooleanSetting("Target Mobs", false);
    private final BooleanSetting weaponsOnly = new BooleanSetting("Weapons Only", false);
    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", false);
    private final BooleanSetting disableOnTarget = new BooleanSetting("Disable on target", false);

    private Entity currentTarget = null;
    private long lastUpdateTime = 0;
    private float currentBaseSpeed = 10f;
    private float nextBaseSpeed = 10f;
    private long lastSpeedChangeTime = 0;
    private float yawVelocity = 0f;
    private float pitchVelocity = 0f;
    private float lastNoiseYaw = 0f;
    private float lastNoisePitch = 0f;

    public AimAssist() {
        super("Aim Assist", "Gives you assistance on your aim", Category.COMBAT);
        addSettings(
                maxYawSpeed, minYawSpeed, maxPitchSpeed, minPitchSpeed, fov, range, smoothing,
                targetPlayers, targetMobs, weaponsOnly, throughWalls, disableOnTarget
        );
    }

    @EventHandler
    private void onRender3D(EventRender3D event) {
        if (isNull()) return;
        if (maxPitchSpeed.getValueFloat() <= minPitchSpeed.getValueFloat()) {
            maxPitchSpeed.setValue(minPitchSpeed.getValue() + 0.1);
        }
        if (maxYawSpeed.getValueFloat() <= minYawSpeed.getValueFloat()) {
            maxYawSpeed.setValue(minYawSpeed.getValue() + 0.1);
        }
        if (weaponsOnly.getValue() && !isHoldingWeapon()) return;

        if (mc.currentScreen != null) return;

        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK
                && mc.options.attackKey.isPressed()) {
            return;
        }

        currentTarget = findBestTarget();

        if (mc.targetedEntity == currentTarget && disableOnTarget.getValue()) {
            return;
        }

        if (currentTarget != null) {
            if (!throughWalls.getValue() && !mc.player.canSee(currentTarget)) return;

            Vec3d chestPos = getChestPosition(currentTarget);
            float[] rotation = calculateRotation(chestPos);
            applySmoothAiming(rotation[0], rotation[1]);
        }
    }

    private Entity findBestTarget() {
        if (isNull()) return null;

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
                double score = distance + (fovDistance * 2.0);
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
        if (Teams.isTeammate(entity)) return false;

        return entity instanceof PlayerEntity ? targetPlayers.getValue() : targetMobs.getValue();
    }

    private Vec3d getChestPosition(Entity entity) {
        return new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
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



    private void applySmoothAiming(float targetYaw, float targetPitch) {
        long currentTime = System.currentTimeMillis();

        if (lastUpdateTime == 0) {
            lastUpdateTime = currentTime;
            return;
        }

        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        if (deltaTime < 0.001f || deltaTime > 0.1f) return;
        deltaTime *= randomFloat(0.9f, 1.1f);

        updateBaseSpeed();

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;

        float distance = (float) Math.hypot(yawDiff, pitchDiff);
        if (distance < 0.3f) return;

        float t = Math.min(distance / 10f, 1f);
        float easedT = easeOutCubic(t);

        float indexOfDifficulty = (float) Math.log(distance / 0.5f + 1f);
        float maxSpeed = indexOfDifficulty * currentBaseSpeed * (smoothing.getValueFloat() / 10f);

        float targetYawChange = yawDiff * easedT;
        float targetPitchChange = pitchDiff * easedT;

        float noiseStrength = MathHelper.clamp(distance / 10f, 0.1f, 1.5f);
        lastNoiseYaw = smoothNoise(lastNoiseYaw, noiseStrength);
        lastNoisePitch = smoothNoise(lastNoisePitch, noiseStrength);

        float freq = randomFloat(40f, 70f);
        float oscillation = (float) Math.sin(currentTime / freq) * 0.3f;

        yawVelocity += (targetYawChange + lastNoiseYaw + oscillation) * deltaTime;
        pitchVelocity += (targetPitchChange + lastNoisePitch + oscillation) * deltaTime;

        float friction = randomFloat(0.65f, 0.75f);
        yawVelocity *= friction;
        pitchVelocity *= friction;

        float velocityMag = (float) Math.hypot(yawVelocity, pitchVelocity);
        if (velocityMag > maxSpeed) {
            float scale = maxSpeed / velocityMag;
            yawVelocity *= scale;
            pitchVelocity *= scale;
        }

        float jitterBreakYaw = randomFloat(-0.0025f, 0.0025f);
        float jitterBreakPitch = randomFloat(-0.0025f, 0.0025f);

        float yawDelta = (float) (yawVelocity * MathUtils.randomDoubleBetween(minYawSpeed.getValue(), maxYawSpeed.getValue()) + jitterBreakYaw);
        float pitchDelta = (float) (pitchVelocity * MathUtils.randomDoubleBetween(minPitchSpeed.getValue(), maxPitchSpeed.getValue()) + jitterBreakPitch);

        float newYaw = currentYaw + yawDelta;
        float newPitch = currentPitch + pitchDelta;

        mc.player.setYaw(newYaw);
        mc.player.setPitch(MathHelper.clamp(newPitch, -89f, 89f));

    }


    private float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }

    private void updateBaseSpeed() {
        long now = System.currentTimeMillis();
        if (now - lastSpeedChangeTime > 200) {
            lastSpeedChangeTime = now;
            float change = randomFloat(-5f, 5f);
            nextBaseSpeed += change;
            nextBaseSpeed = MathHelper.clamp(nextBaseSpeed, 8f, 100f);
        }

        currentBaseSpeed = (currentBaseSpeed * 0.9f) + (nextBaseSpeed * 0.1f);
    }

    private float smoothNoise(float previous, float strength) {
        return previous * 0.8f + randomFloat(-strength, strength) * 0.2f;
    }

    private float randomFloat(float min, float max) {
        return min + (float) Math.random() * (max - min);
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