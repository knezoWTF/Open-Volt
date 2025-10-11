package com.volt.utils.mc;

import com.volt.IMinecraft;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@UtilityClass
@Getter
public final class PlayerUtil implements IMinecraft {
    private int offGroundTicks = 0;
    private int groundTicks = 0;

    public int getOffGroundTicks() {
        assert mc.player != null;
        if (mc.player.isOnGround()) {
            groundTicks++;
            offGroundTicks = 0;
        } else {
            groundTicks = 0;
            offGroundTicks++;
        }
        return offGroundTicks;
    }

    public static boolean isLookingAt(BlockPos pos, double maxDistance) {
        if (mc.player == null || mc.world == null) return false;

        Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        Vec3d reachVec = eyePos.add(lookVec.multiply(maxDistance));

        BlockHitResult result = mc.world.raycast(new net.minecraft.world.RaycastContext(
                eyePos,
                reachVec,
                net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        return result != null && result.getBlockPos().equals(pos);
    }
}
