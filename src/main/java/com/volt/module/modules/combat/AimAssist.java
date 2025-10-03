package com.volt.module.modules.combat;

import com.volt.event.impl.render.EventRender3D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.modules.misc.Teams;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;

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

    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 10.0, 5.0, 0.1);
    private final NumberSetting fov = new NumberSetting("FOV", 10.0, 180.0, 90.0, 1.0);
    private final NumberSetting range = new NumberSetting("Range", 1.0, 10.0, 4.5, 0.1);
    private final NumberSetting pitchSpeed = new NumberSetting("Pitch Speed", 0.1, 5.0, 2.0, 0.1);
    private final NumberSetting yawSpeed = new NumberSetting("Yaw Speed", 0.1, 5.0, 2.0, 0.1);
    private final NumberSetting smoothing = new NumberSetting("Smoothing", 1.0, 20.0, 10.0, 0.5);

    private final BooleanSetting targetPlayers = new BooleanSetting("Target Players", true);
    private final BooleanSetting targetMobs = new BooleanSetting("Target Mobs", false);
    private final BooleanSetting weaponsOnly = new BooleanSetting("Weapons Only", false);
    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", false);

    private Entity currentTarget = null;
    private long lastUpdateTime = 0;

    public AimAssist() {
        super("Aim Assist", "Gives you assistance on your aim", Category.COMBAT);
        addSettings(
                speed, fov, range, pitchSpeed, yawSpeed, smoothing,
                targetPlayers, targetMobs, weaponsOnly, throughWalls
        );
    }

@EventHandler
private void onRender3D(EventRender3D event) {
    if (isNull()) return;


    if (weaponsOnly.getValue() && !isHoldingWeapon()) return;

    if (mc.currentScreen != null) return;

    if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK
        && mc.options.attackKey.isPressed()) {
        return;
    }

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
    
    float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000.0f, 0.05f);
    lastUpdateTime = currentTime;
    
    if (deltaTime < 0.001f) return;

    float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
    float pitchDiff = targetPitch - mc.player.getPitch();

    float distance = (float) Math.hypot(yawDiff, pitchDiff);
    
    if (distance < 0.3f) return;
    
    float speedFactor = speed.getValueFloat() * 0.1f;
    float smoothingFactor = smoothing.getValueFloat();
    
    float baseInterpolation = speedFactor / smoothingFactor;
    
    float yawStep = yawDiff * baseInterpolation * yawSpeed.getValueFloat();
    float pitchStep = pitchDiff * baseInterpolation * pitchSpeed.getValueFloat();
    
    float frameCorrection = deltaTime * 60.0f;
    yawStep *= frameCorrection;
    pitchStep *= frameCorrection;
    
    float maxStep = speedFactor * 3.0f;
    yawStep = MathHelper.clamp(yawStep, -maxStep, maxStep);
    pitchStep = MathHelper.clamp(pitchStep, -maxStep, maxStep);

    mc.player.setYaw(mc.player.getYaw() + yawStep);
    mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + pitchStep, -89f, 89f));
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
