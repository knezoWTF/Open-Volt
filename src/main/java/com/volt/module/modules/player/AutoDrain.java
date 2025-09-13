package com.volt.module.modules.player;


import org.lwjgl.glfw.GLFW;

import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.MouseSimulation;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

// TO:DO possibly make a better if in cobweb check but rn AABB is the best i can think of
 

public final class AutoDrain extends Module {

    private static final NumberSetting actionCooldownMs = new NumberSetting("Cooldown MS", 50, 2000, 250, 1);
    private static final NumberSetting switchBackDelayMs = new NumberSetting("SwitchBack MS", 0, 500, 75, 1);

    private final TimerUtil cooldownTimer = new TimerUtil();
    private final TimerUtil switchBackTimer = new TimerUtil();
    private int originalSlot = -1;
    private boolean pendingSwitchBack = false;

    public AutoDrain() {
        super("Auto Drain", "Swap to empty bucket and pick up water when aiming at a source", -1, Category.PLAYER);
        this.addSettings(actionCooldownMs, switchBackDelayMs);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || mc.currentScreen != null || mc.world == null) return;

        if (pendingSwitchBack) {
            if (switchBackDelayMs.getValueInt() <= 0 || switchBackTimer.hasElapsedTime(switchBackDelayMs.getValueInt())) {
                if (originalSlot != -1) mc.player.getInventory().selectedSlot = originalSlot;
                originalSlot = -1;
                pendingSwitchBack = false;
            }
            return;
        }

        if (!cooldownTimer.hasElapsedTime(actionCooldownMs.getValueInt())) return;

        if (!(mc.crosshairTarget instanceof BlockHitResult blockHit)) return;
        BlockPos waterPos = getWaterSourcePosFromMouseOver(blockHit);
        if (waterPos == null) return;

        if (isPlayerInCobweb()) return;

        int emptyBucketSlot = findEmptyBucketInHotbar();
        if (emptyBucketSlot == -1) return;

        originalSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = emptyBucketSlot;
    ((MinecraftClientAccessor) mc).invokeDoItemUse();
    MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        pendingSwitchBack = true;
        switchBackTimer.reset();
        cooldownTimer.reset();
    }

    private boolean isWaterSource(FluidState fluidState) {
        return fluidState != null && fluidState.getFluid() == Fluids.WATER && fluidState.isStill();
    }

    

    private int findEmptyBucketInHotbar() {
        for (int hotbarIndex = 0; hotbarIndex < 9; hotbarIndex++) {
            ItemStack stack = mc.player.getInventory().getStack(hotbarIndex);
            if (!stack.isEmpty() && stack.getItem() == Items.BUCKET) return hotbarIndex;
        }
        return -1;
    }

    private boolean isPlayerInCobweb() {
        var box = mc.player.getBoundingBox(); // this seems messy but otherwise it dosnt work on the edge of the cobweb
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.floor(box.maxX);
        int maxY = (int) Math.floor(box.maxY);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (mc.world.getBlockState(new BlockPos(x, y, z)).isOf(Blocks.COBWEB)) return true;
                }
            }
        }
        return false;
    }

    private BlockPos getWaterSourcePosFromMouseOver(BlockHitResult blockHit) {
        BlockPos hitPos = blockHit.getBlockPos();
        BlockState hitState = mc.world.getBlockState(hitPos);
        if (hitState.getBlock() == Blocks.WATER && isWaterSource(mc.world.getFluidState(hitPos))) {
            return hitPos;
        }
        BlockPos towardPlayer = hitPos.offset(blockHit.getSide());
        BlockState towardState = mc.world.getBlockState(towardPlayer);
        if (towardState.getBlock() == Blocks.WATER && isWaterSource(mc.world.getFluidState(towardPlayer))) {
            return towardPlayer;
        }
        return null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (pendingSwitchBack && originalSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
        originalSlot = -1;
        pendingSwitchBack = false;
    }
}


