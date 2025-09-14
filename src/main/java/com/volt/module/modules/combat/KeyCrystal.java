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
import com.volt.utils.mc.InventoryUtil;
import com.volt.utils.mc.MouseSimulation;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public final class KeyCrystal extends Module {

    private final KeybindSetting crystalKeybind = new KeybindSetting("Crystal Key", GLFW.GLFW_MOUSE_BUTTON_4, false);
    private final NumberSetting delay = new NumberSetting("Delay (MS)", 1, 100, 10, 1);
    private final BooleanSetting antiSuicide = new BooleanSetting("Anti Suicide", true);
    private final BooleanSetting mouseSimulation = new BooleanSetting("Mouse Simulation", true);

    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil attackTimer = new TimerUtil();

    private boolean keyPressed = false;
    private boolean isActive = false;
    private int originalSlot = -1;
    private boolean hasPlacedObsidian = false;

    public KeyCrystal() {
        super("Key Crystal", "Automatically places and explodes crystals and obsidian for PvP", -1, Category.COMBAT);
        this.addSettings(crystalKeybind, delay, antiSuicide, mouseSimulation);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(crystalKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull())
            return;
        if (mc.currentScreen != null)
            return;
        boolean currentKeyState = KeyUtils.isKeyPressed(crystalKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            startCrystalPvP();
        } else if (!currentKeyState && keyPressed) {
            stopCrystalPvP();
        }

        keyPressed = currentKeyState;

        if (isActive && timer.hasElapsedTime(delay.getValueInt())) {
            processCrystalPvP();
            timer.reset();
        }
    }

    ;

    private void startCrystalPvP() {
        if (isActive)
            return;
        isActive = true;
        originalSlot = mc.player.getInventory().selectedSlot;
        hasPlacedObsidian = false;
        timer.reset();
    }

    private void stopCrystalPvP() {
        if (!isActive)
            return;
        if (originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
        isActive = false;
        originalSlot = -1;
        hasPlacedObsidian = false;
    }

    private void processCrystalPvP() {
        if (antiSuicide.getValue() && !mc.player.isOnGround()) {
            return;
        }

        if (mc.crosshairTarget instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof EndCrystalEntity crystal) {

                if (mc.player.getPos().distanceTo(crystal.getPos()) <= 6.0 && attackTimer.hasElapsedTime(150)) {
                    if (mouseSimulation.getValue()) {
                        MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
                    } else {
                        ((MinecraftClientAccessor) mc).invokeDoAttack();
                    }
                    attackTimer.reset();
                }
                return;
            }
        }

        if (mc.crosshairTarget instanceof BlockHitResult blockHit) {
            BlockPos targetBlock = blockHit.getBlockPos();
            BlockPos placementPos = targetBlock.offset(blockHit.getSide());

            if (isObsidianOrBedrock(targetBlock) && isValidCrystalPosition(placementPos)) {
                if (hasItemInHotbar(Items.END_CRYSTAL)) {
                    InventoryUtil.swapToSlot(Items.END_CRYSTAL);
                    if (mouseSimulation.getValue()) {
                        MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                    } else {
                        ((MinecraftClientAccessor) mc).invokeDoItemUse();
                    }
                }
            } else if (isValidPosition(placementPos) && !hasPlacedObsidian) {
                BlockPos below = placementPos.down();
                if (!mc.world.getBlockState(below).isAir()) {
                    if (hasItemInHotbar(Items.OBSIDIAN)) {
                        InventoryUtil.swapToSlot(Items.OBSIDIAN);
                        if (mouseSimulation.getValue()) {
                            MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
                        } else {
                            ((MinecraftClientAccessor) mc).invokeDoItemUse();
                        }
                        hasPlacedObsidian = true;
                    }
                }
            }
        }
    }

    private boolean hasItemInHotbar(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (i >= 0 && i < 9) {
                var stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() == item) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidPosition(BlockPos pos) {
        if (mc.world == null)
            return false;
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 4.5)
            return false;
        if (!mc.world.getBlockState(pos).isAir())
            return false;

        BlockPos playerPos = mc.player.getBlockPos();
        return !pos.equals(playerPos) && !pos.equals(playerPos.up());
    }

    private boolean isObsidianOrBedrock(BlockPos pos) {
        if (mc.world == null)
            return false;
        var block = mc.world.getBlockState(pos).getBlock();
        return block == net.minecraft.block.Blocks.OBSIDIAN || block == net.minecraft.block.Blocks.BEDROCK;
    }

    private boolean isValidCrystalPosition(BlockPos pos) {
        if (mc.world == null)
            return false;
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > 4.5)
            return false;

        if (!mc.world.getBlockState(pos).isAir())
            return false;

        if (!mc.world.getBlockState(pos.up()).isAir())
            return false;

        BlockPos playerPos = mc.player.getBlockPos();
        return !pos.equals(playerPos) && !pos.equals(playerPos.up()) && !pos.up().equals(playerPos)
                && !pos.up().equals(playerPos.up());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stopCrystalPvP();
    }

    @Override
    public int getKey() {
        return -1;
    }
}