package com.volt.module.modules.misc;

import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.MouseSimulation;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public final class PearlCatch extends Module {
    private final KeybindSetting pearlChargeKeybind = new KeybindSetting("Pearl Charge Key", GLFW.GLFW_KEY_H, true);
    private final BooleanSetting silentMode = new BooleanSetting("Silent", true);
    
    private final TimerUtil pearlDelayTimer = new TimerUtil();
    private boolean keyPressed = false;
    private boolean pearlThrown = false;
    private int originalSlot = -1;
    private boolean needsSlotRestore = false;

    public PearlCatch() {
        super("Pearl Catch", "Throws pearl then windcharge", -1, Category.MISC);
        this.addSettings(pearlChargeKeybind, silentMode);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(pearlChargeKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;
        
        boolean currentKeyState = KeyUtils.isKeyPressed(pearlChargeKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            handlePearlChargeSequence();
        }

        if (pearlThrown && pearlDelayTimer.hasElapsedTime(200)) {
            throwWindCharge();
            pearlThrown = false;
        }

        if (needsSlotRestore && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
            needsSlotRestore = false;
            originalSlot = -1;
        }

        keyPressed = currentKeyState;
    }

    private void handlePearlChargeSequence() {
        int pearlSlot = findPearlInHotbar();
        if (pearlSlot == -1) return;

        if (silentMode.getValue()) {
            throwPearlSilently(pearlSlot);
        } else {
            throwPearlNormally(pearlSlot);
        }

        pearlThrown = true;
        pearlDelayTimer.reset();
    }

    private void throwWindChargeSilently(int windChargeSlot) {
        originalSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = windChargeSlot;
        
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        
        needsSlotRestore = true;
    }

    private void throwWindChargeNormally(int windChargeSlot) {
        originalSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = windChargeSlot;
        
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        
        needsSlotRestore = true;
    }

    private void throwWindCharge() {
        int windChargeSlot = findWindChargeInHotbar();
        if (windChargeSlot == -1) return;

        if (silentMode.getValue()) {
            throwWindChargeSilently(windChargeSlot);
        } else {
            throwWindChargeNormally(windChargeSlot);
        }
    }

    private void throwPearlSilently(int pearlSlot) {
        int currentSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = pearlSlot;
        
        mc.player.swingHand(Hand.MAIN_HAND);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        
        mc.player.getInventory().selectedSlot = currentSlot;
    }

    private void throwPearlNormally(int pearlSlot) {
        mc.player.getInventory().selectedSlot = pearlSlot;
        
        mc.player.swingHand(Hand.MAIN_HAND);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
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

    private int findPearlInHotbar() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        keyPressed = false;
        pearlThrown = false;
        originalSlot = -1;
        needsSlotRestore = false;
        pearlDelayTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        pearlThrown = false;
        needsSlotRestore = false;
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
}