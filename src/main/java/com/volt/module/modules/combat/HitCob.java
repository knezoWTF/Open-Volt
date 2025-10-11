package com.volt.module.modules.combat;

import com.volt.event.impl.player.AttackEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class HitCob extends Module {
    private static final double SPEED_STOPPED = 0.05;
    private static final double SPEED_WALKING = 0.13;
    private static final double SPEED_SPRINTING = 0.26;

    public HitCob() {
        super("Hit Cob", "Places cobweb at player feet when you hit them (DO NOT USE ITS BEING TESTED)", Category.COMBAT);
    }

    @EventHandler
    private void onAttack(AttackEvent event) {
        if (isNull() || !(event.getTarget() instanceof PlayerEntity target)) return;

        int webSlot = findCobwebInHotbar();
        if (webSlot == -1) return;

        BlockPos feetPos = target.getBlockPos();
        
        if (mc.world.getBlockState(feetPos).getBlock() == Blocks.COBWEB) return;
        if (mc.world.getBlockState(feetPos).getBlock() == Blocks.WATER) return;
        
        double distance = mc.player.getPos().distanceTo(Vec3d.ofCenter(feetPos));
        if (distance > 4.5) return;

        double targetSpeed = Math.hypot(target.getVelocity().x, target.getVelocity().z);
        
        Vec3d knockbackDirection = target.getPos().subtract(mc.player.getPos()).normalize();
        
        double knockbackDistance;
        if (targetSpeed < SPEED_STOPPED) {
            knockbackDistance = mc.player.isSprinting() ? 1.8 : 1.2;
        } else if (targetSpeed < SPEED_WALKING) {
            knockbackDistance = mc.player.isSprinting() ? 1.5 : 1.0;
        } else {
            knockbackDistance = mc.player.isSprinting() ? 1.2 : 0.8;
        }
        
        Vec3d predictedPos = target.getPos().add(knockbackDirection.multiply(knockbackDistance));
        BlockPos predictedFeet = BlockPos.ofFloored(predictedPos);
        
        if (mc.world.getBlockState(predictedFeet).getBlock() == Blocks.COBWEB) return;
        if (mc.world.getBlockState(predictedFeet).getBlock() == Blocks.WATER) return;
        
        if (!mc.world.getBlockState(predictedFeet).isAir()) {
            predictedFeet = feetPos;
        }
        
        BlockPos groundPos = predictedFeet.down();
        if (mc.world.getBlockState(groundPos).isAir()) return;
        
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(predictedFeet)) > 4.5) return;

        int originalSlot = mc.player.getInventory().selectedSlot;
        float originalYaw = mc.player.getYaw();
        float originalPitch = mc.player.getPitch();

        Vec3d hitVec = Vec3d.ofCenter(groundPos).add(0, 0.5, 0);
        float[] rotation = calculateRotation(hitVec);
        
        mc.player.setYaw(rotation[0]);
        mc.player.setPitch(rotation[1]);
        mc.player.getInventory().selectedSlot = webSlot;

        BlockHitResult hitResult = new BlockHitResult(
            hitVec,
            Direction.UP,
            groundPos,
            false
        );

        if (mc.interactionManager != null) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        }

        mc.player.getInventory().selectedSlot = originalSlot;
        mc.player.setYaw(originalYaw);
        mc.player.setPitch(originalPitch);
    }

    private int findCobwebInHotbar() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.COBWEB) {
                return i;
            }
        }
        return -1;
    }

    private float[] calculateRotation(Vec3d target) {
        Vec3d diff = target.subtract(mc.player.getEyePos());
        double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distance));
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -89.0f, 89.0f)};
    }
}

