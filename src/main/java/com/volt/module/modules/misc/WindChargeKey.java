package com.volt.module.modules.misc;


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
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public final class WindChargeKey extends Module {
    private final KeybindSetting windChargeKeybind = new KeybindSetting("Wind Charge Key", GLFW.GLFW_KEY_G, true);
    private final NumberSetting switchDelayMS = new NumberSetting("Switch Delay (MS)", 1, 500, 50, 1);
    private final NumberSetting throwDelayMS = new NumberSetting("Throw Delay (MS)", 50, 1000, 200, 25);
    private final BooleanSetting silentMode = new BooleanSetting("Silent", false);
    private final BooleanSetting autoJump = new BooleanSetting("Auto Jump", true);
    private final NumberSetting jumpDelayMS = new NumberSetting("Jump Delay (MS)", 0, 100, 10, 5);
    private final TimerUtil throwTimer = new TimerUtil();
    private final TimerUtil switchBackTimer = new TimerUtil();
    private final TimerUtil jumpTimer = new TimerUtil();
    private boolean keyPressed = false;
    private int originalSlot = -1;
    private boolean needsSlotRestore = false;
    private boolean firstThrow = true;
    private boolean jumpExecuted = false;

    public WindChargeKey() {
        super("Wind Charge Key", "Automatically throws wind charges", -1, Category.MISC);
        this.addSettings(windChargeKeybind, switchDelayMS, throwDelayMS, silentMode, autoJump, jumpDelayMS);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(windChargeKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;
        boolean currentKeyState = KeyUtils.isKeyPressed(windChargeKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            handleWindChargeThrow();
        }


        if (jumpExecuted && jumpTimer.hasElapsedTime(jumpDelayMS.getValueInt())) {
            if (autoJump.getValue() && mc.player.isOnGround()) {
                mc.player.jump();
            }
            jumpExecuted = false;
        }

        if (needsSlotRestore && switchBackTimer.hasElapsedTime(switchDelayMS.getValueInt())) {
            if (originalSlot != -1 && originalSlot != mc.player.getInventory().selectedSlot) {
                mc.player.getInventory().selectedSlot = originalSlot;
            }
            needsSlotRestore = false;
            originalSlot = -1;
        }

        keyPressed = currentKeyState;
    }

    ;

    private void handleWindChargeThrow() {
        if (!firstThrow && !throwTimer.hasElapsedTime(throwDelayMS.getValueInt())) {
            return;
        }

        int windChargeSlot = findWindChargeInHotbar();
        if (windChargeSlot == -1) return;


        if (autoJump.getValue() && mc.player.isOnGround()) {
            if (jumpDelayMS.getValueInt() == 0) {

                mc.player.jump();
            } else {

                jumpExecuted = true;
                jumpTimer.reset();
            }
        }

        if (silentMode.getValue()) {
            throwWindChargeSilently(windChargeSlot);
        } else {
            throwWindChargeNormally(windChargeSlot);
        }

        firstThrow = false;
        throwTimer.reset();
    }

    private void throwWindChargeSilently(int windChargeSlot) {
        int currentSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = windChargeSlot;

    ((MinecraftClientAccessor) mc).invokeDoItemUse();
    MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

        mc.player.getInventory().selectedSlot = currentSlot;
    }

    private void throwWindChargeNormally(int windChargeSlot) {
        originalSlot = mc.player.getInventory().selectedSlot;

        mc.player.getInventory().selectedSlot = windChargeSlot;

    ((MinecraftClientAccessor) mc).invokeDoItemUse();
    MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);

        needsSlotRestore = true;
        switchBackTimer.reset();
    }

    private int findWindChargeInHotbar() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.WIND_CHARGE) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public void onEnable() {
        keyPressed = false;
        originalSlot = -1;
        needsSlotRestore = false;
        firstThrow = true;
        jumpExecuted = false;
        throwTimer.reset();
        switchBackTimer.reset();
        jumpTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
}
