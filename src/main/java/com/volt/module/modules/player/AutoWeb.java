package com.volt.module.modules.player;


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

import java.security.SecureRandom;

public final class AutoWeb extends Module {
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

    public AutoWeb() {
        super("Auto Web", "Hold keybind to place cobwebs quickly", -1, Category.PLAYER);
        this.addSettings(webKeybind, webCount, clickDelayMS, randomizeDelay, autoSwitch);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(webKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (mc.player == null || mc.world == null)
            return;

        boolean currentKeyState = KeyUtils.isKeyPressed(webKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            startPlacing();
        } else if (!currentKeyState && keyPressed) {
            stopPlacing();
        }

        keyPressed = currentKeyState;

        if (!isActive)
            return;

        long delay = clickDelayMS.getValueInt();
        if (randomizeDelay.getValue()) {
            delay = random.nextLong(delay / 2, delay * 2);
        }

        if (!clickTimer.hasElapsedTime(delay))
            return;

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
            ((MinecraftClientAccessor) mc).invokeDoItemUse();
            MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            websPlaced++;
            clickTimer.reset();
        } else {
            stopPlacing();
        }
    }

    ;

    private void startPlacing() {
        if (isActive) return;

        int webSlot = findCobwebInHotbar();
        if (webSlot == -1) return;

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

    @Override
    public void onEnable() {
        keyPressed = false;
        isActive = false;
        originalSlot = -1;
        websPlaced = 0;
        hasSwitchedToWeb = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (isActive) {
            stopPlacing();
        }
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
}
