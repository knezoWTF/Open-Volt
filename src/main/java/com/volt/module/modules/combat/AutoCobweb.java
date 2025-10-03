package com.volt.module.modules.combat;

import com.volt.event.impl.player.EventAttack;
import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;

import java.security.SecureRandom;

public final class AutoCobweb extends Module {
    private final KeybindSetting webKeybind = new KeybindSetting("Web Key", GLFW.GLFW_KEY_Z, false);
    private final NumberSetting webCount = new NumberSetting("Web Count", 1, 10, 3, 1);
    private final NumberSetting clickDelayMS = new NumberSetting("Click Delay (MS)", 10, 200, 50, 10);
    private final BooleanSetting randomizeDelay = new BooleanSetting("Randomize Delay", true);
    private final BooleanSetting autoSwitch = new BooleanSetting("Auto Switch Back", true);

    private final TimerUtil clickTimer = new TimerUtil();
    private final SecureRandom random = new SecureRandom();
    private boolean keyPressed = false;
    private boolean isActive = false;
    private int originalSlot = -1;
    private int websPlaced = 0;
    private int targetWebCount = 0;
    private boolean hasSwitchedToWeb = false;

    private BlockPos targetBlockPos = null;
    private boolean triggeredByAttack = false;

    public AutoCobweb() {
        super("Auto CobWeb", "Places cobwebs under players or by keybind", -1, Category.COMBAT);
        this.addSettings(webKeybind, webCount, clickDelayMS, randomizeDelay, autoSwitch);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(webKeybind));
    }

    @EventHandler
    private void onAttack(EventAttack event) {
        if (mc.player == null || mc.world == null) return;
        targetBlockPos = event.getTarget().getBlockPos().down();

        if (canPlaceCobweb(targetBlockPos)) {
            triggeredByAttack = true;
            startPlacing();
        }
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        boolean currentKeyState = KeyUtils.isKeyPressed(webKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            targetBlockPos = mc.player.getBlockPos();
            triggeredByAttack = false;
            startPlacing();
        } else if (!currentKeyState && keyPressed && !triggeredByAttack) {
            stopPlacing();
        }

        keyPressed = currentKeyState;

        if (!isActive) return;

        long delay = clickDelayMS.getValueInt();
        if (randomizeDelay.getValue()) {
            delay = random.nextLong(delay / 2, delay * 2);
        }

        if (!clickTimer.hasElapsedTime(delay)) return;

        if (!hasSwitchedToWeb) {
            int webSlot = findCobwebInHotbar();
            if (webSlot == -1) {
                stopPlacing();
                return;
            }

            originalSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = webSlot;
            hasSwitchedToWeb = true;
            clickTimer.reset();
            return;
        }

        if (websPlaced < targetWebCount) {
            placeCobweb();
            websPlaced++;
            clickTimer.reset();
        } else {
            stopPlacing();
        }
    }

    private void lookAtBlockTop(BlockPos pos) {
        Vec3d target = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.95, pos.getZ() + 0.5);
        lookAt(target);
    }


    private void placeCobweb() {
        if (targetBlockPos == null || !canPlaceCobweb(targetBlockPos)) return;

        lookAtBlockTop(targetBlockPos);

        ((MinecraftClientAccessor) mc).invokeDoItemUse();
    }

    private void startPlacing() {
        if (isActive) return;

        int webSlot = findCobwebInHotbar();
        if (webSlot == -1 || targetBlockPos == null) return;

        isActive = true;
        websPlaced = 0;
        targetWebCount = webCount.getValueInt();
        hasSwitchedToWeb = false;
        clickTimer.reset();
    }

    private void stopPlacing() {
        if (!isActive) return;

        if (autoSwitch.getValue() && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }

        isActive = false;
        originalSlot = -1;
        websPlaced = 0;
        hasSwitchedToWeb = false;
        triggeredByAttack = false;
        targetBlockPos = null;
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

    private boolean canPlaceCobweb(BlockPos pos) {
        var state = mc.world.getBlockState(pos);
        return state.isReplaceable() || state.isAir() || state.getBlock() == Blocks.AIR;
    }

    private void lookAt(Vec3d pos) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d diff = pos.subtract(eyes);
        double distXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distXZ));

        mc.player.setYaw(MathHelper.wrapDegrees(yaw));
        mc.player.setPitch(MathHelper.clamp(pitch, -89f, 89f));
    }

    @Override
    public void onEnable() {
        keyPressed = false;
        isActive = false;
        originalSlot = -1;
        websPlaced = 0;
        hasSwitchedToWeb = false;
        triggeredByAttack = false;
        targetBlockPos = null;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        stopPlacing();
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
}
