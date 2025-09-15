package com.volt.module.modules.movement;

import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public final class AutoHeadHitter extends Module {
    private final NumberSetting jumpDelay = new NumberSetting("Jump Delay", 0, 500, 100, 10);
    private final BooleanSetting holdingSpace = new BooleanSetting("Holding Space", false);

    private final TimerUtil jumpTimer = new TimerUtil();

    public AutoHeadHitter() {
        super("Auto Head Hitter", "Auto jumps when there's a solid block above to make u go fast", -1, Category.MOVEMENT);
        this.addSettings(jumpDelay, holdingSpace);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (holdingSpace.getValue() && !mc.options.jumpKey.isPressed()) return;

        if (jumpDelay.getValueInt() > 0 && !jumpTimer.hasElapsedTime(jumpDelay.getValueInt())) return;

        if (!mc.player.isOnGround()) return;

        BlockPos playerPos = mc.player.getBlockPos();
        BlockPos headPos = playerPos.up(2);

        BlockState blockState = mc.world.getBlockState(headPos);

        if (!blockState.isAir() && blockState.getBlock() != Blocks.WATER && blockState.getBlock() != Blocks.LAVA) {
            mc.player.jump();
            jumpTimer.reset();
        }
    }
}
        