package com.volt.module.modules.combat;

import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.MouseSimulation;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class KeyAnchor extends Module {

    private final KeybindSetting anchorKeybind = new KeybindSetting("Anchor Key", GLFW.GLFW_KEY_X, false);
    private final NumberSetting delay = new NumberSetting("Delay (MS)", 1, 500, 50, 1);
    private final NumberSetting restoreDelayTicks = new NumberSetting("Restore Delay", 1, 20, 2, 1);
    private final BooleanSetting mouseSimulation = new BooleanSetting("Mouse Simulation", true);

    private final TimerUtil timer = new TimerUtil();
    private boolean keyPressed = false;
    private boolean isActive = false;
    private int originalSlot = -1;
    private boolean hasPlacedThisCycle = false;
    private boolean pendingRestoreSlot = false;
    private int pendingRestoreTicksLeft = 0;

    public KeyAnchor() {
        super("Key Anchor", "Automatically places and explodes respawn anchors for PvP", -1, Category.COMBAT);
        this.addSettings(anchorKeybind, delay, restoreDelayTicks, mouseSimulation);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(anchorKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || !isEnabled()) return;
        if (mc.currentScreen != null) return;

        boolean currentKeyState = KeyUtils.isKeyPressed(anchorKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            startAnchorPvP();
        } else if (!currentKeyState && keyPressed) {
            stopAnchorPvP();
        } else if (!currentKeyState) {
            hasPlacedThisCycle = false;
        }

        keyPressed = currentKeyState;

        if (isActive && timer.hasElapsedTime(delay.getValueInt())) {
            processAnchorPvP();
            timer.reset();
        }

        if (pendingRestoreSlot) {
            if (pendingRestoreTicksLeft <= 0) {
                restoreOriginalSlot();
                pendingRestoreSlot = false;
            } else {
                pendingRestoreTicksLeft--;
            }
        }
    }

    private void startAnchorPvP() {
        if (isActive) return;

        isActive = true;
        originalSlot = mc.player.getInventory().selectedSlot;
        hasPlacedThisCycle = false;
        timer.reset();
    }

    private void stopAnchorPvP() {
        if (!isActive) return;

        if (originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
        isActive = false;
        originalSlot = -1;
        pendingRestoreSlot = false;
        pendingRestoreTicksLeft = 0;
    }

    private void processAnchorPvP() {
        if (mc.crosshairTarget instanceof BlockHitResult blockHit) {
            BlockPos targetBlock = blockHit.getBlockPos();
            var blockState = mc.world.getBlockState(targetBlock);

            if (blockState.getBlock() == Blocks.RESPAWN_ANCHOR) {
                int charges = blockState.get(RespawnAnchorBlock.CHARGES);

                if (charges == 0) {
                    if (swapToItem(Items.GLOWSTONE)) {
                        hasPlacedThisCycle = true;
                        if (mouseSimulation.getValue()) {
                            ((MinecraftClientAccessor) mc).setItemUseCooldown(0);
                            MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                        } else {
                            ((MinecraftClientAccessor) mc).invokeDoItemUse();
                        }
                    }
                } else {
                    if (swapToSword()) {
                        if (mouseSimulation.getValue()) {
                            ((MinecraftClientAccessor) mc).setItemUseCooldown(0);
                            MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                        } else {
                            ((MinecraftClientAccessor) mc).invokeDoItemUse();
                        }
                        scheduleRestoreOriginalSlot();
                    } else if (swapToItem(Items.TOTEM_OF_UNDYING)) {
                        if (mouseSimulation.getValue()) {
                            ((MinecraftClientAccessor) mc).setItemUseCooldown(0);
                            MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                        } else {
                            ((MinecraftClientAccessor) mc).invokeDoItemUse();
                        }
                        scheduleRestoreOriginalSlot();
                    }
                    hasPlacedThisCycle = true;
                }
                return;
            }

            BlockPos placementPos = targetBlock.offset(blockHit.getSide());
            if (isValidAnchorPosition(placementPos) && !hasPlacedThisCycle) {
                if (swapToItem(Items.RESPAWN_ANCHOR)) {
                    hasPlacedThisCycle = true;
                    if (mouseSimulation.getValue()) {
                        ((MinecraftClientAccessor) mc).setItemUseCooldown(0);
                        MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                    } else {
                        ((MinecraftClientAccessor) mc).invokeDoItemUse();
                    }
                }
            }
        }
    }

    private boolean isValidAnchorPosition(BlockPos pos) {
        if (mc.world == null || mc.player == null) return false;
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 4.5) return false;
        if (!mc.world.getBlockState(pos).isAir()) return false;

        BlockPos playerPos = mc.player.getBlockPos();
        return !pos.equals(playerPos) && !pos.equals(playerPos.up());
    }

    private boolean swapToItem(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                mc.player.getInventory().selectedSlot = i;
                return true;
            }
        }
        return false;
    }

    private boolean swapToSword() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SwordItem) {
                mc.player.getInventory().selectedSlot = i;
                return true;
            }
        }
        return false;
    }

    private void restoreOriginalSlot() {
        if (originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
    }

    private void scheduleRestoreOriginalSlot() {
        if (originalSlot != -1) {
            pendingRestoreSlot = true;
            pendingRestoreTicksLeft = restoreDelayTicks.getValueInt();
        }
    }

    @Override
    public void onEnable() {
        keyPressed = false;
        isActive = false;
        originalSlot = -1;
        hasPlacedThisCycle = false;
        pendingRestoreSlot = false;
        pendingRestoreTicksLeft = 0;
        timer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        stopAnchorPvP();
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
}