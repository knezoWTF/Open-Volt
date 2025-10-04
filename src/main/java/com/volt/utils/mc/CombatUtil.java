package com.volt.utils.mc;

import com.volt.IMinecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public final class CombatUtil implements IMinecraft {
    public static boolean isShieldFacingAway(final LivingEntity en) {

        if (en == null) return true;
        if (!en.isPlayer()) return true;
        if (mc.player == null) return false;

        Vec3d toLocal = mc.player.getPos().subtract(en.getPos());
        if (toLocal.lengthSquared() == 0) return true;
        toLocal = toLocal.normalize();

        final double yaw = Math.toRadians(en.getYaw());
        final double pitch = Math.toRadians(en.getPitch());

        Vec3d facing = new Vec3d(
                -Math.sin(yaw) * Math.cos(pitch),
                -Math.sin(pitch),
                Math.cos(yaw) * Math.cos(pitch)
        ).normalize();

        double dot = facing.dotProduct(toLocal);
        return dot < -0.06;
    }
}
