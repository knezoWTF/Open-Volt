package com.volt.module.modules.misc;


import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public final class PearlKey extends Module {
    private final KeybindSetting pearlKeybind = new KeybindSetting("Pearl Key", GLFW.GLFW_KEY_P, true);
    private final NumberSetting switchDelayMS = new NumberSetting("Switch Delay (MS)", 1, 500, 50, 1);
    private final NumberSetting throwDelayMS = new NumberSetting("Throw Delay (MS)", 100, 5000, 1000, 50);
    private final BooleanSetting silentMode = new BooleanSetting("Silent", false);
    private final TimerUtil throwTimer = new TimerUtil();
    private boolean keyPressed = false;
    private int originalSlot = -1;

    public PearlKey() {
        super("Pearl Key", "Automatically throws ender pearls when hotkey is pressed", -1, Category.MISC);
        this.addSettings(pearlKeybind, switchDelayMS, throwDelayMS, silentMode);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(pearlKeybind));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;
        boolean currentKeyState = KeyUtils.isKeyPressed(pearlKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            handlePearlThrow();
        }

        keyPressed = currentKeyState;
    }

    private void handlePearlThrow() {
        if (!throwTimer.hasElapsedTime(throwDelayMS.getValueInt(), false)) {
            return;
        }

        int pearlSlot = findPearlInHotbar();
        if (pearlSlot == -1) {
            return;
        }

        if (silentMode.getValue()) {
            throwPearlSilently(pearlSlot);
        } else {
            throwPearlNormally(pearlSlot);
        }

        throwTimer.reset();
    }

    private void throwPearlSilently(int pearlSlot) {
        int currentSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = pearlSlot;

        if (mc.interactionManager != null) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }

        mc.player.getInventory().selectedSlot = currentSlot;
    }

    private void throwPearlNormally(int pearlSlot) {
        originalSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = pearlSlot;

        if (mc.interactionManager != null) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }

        scheduleSlotRestore();
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

    private void scheduleSlotRestore() {
        new Thread(() -> {
            try {
                Thread.sleep(switchDelayMS.getValueInt());
                if (mc.player != null && originalSlot != -1) {
                    mc.player.getInventory().selectedSlot = originalSlot;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void onEnable() {
        keyPressed = false;
        originalSlot = -1;
        throwTimer.reset();
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

    @Override
    public void setKey(int key) {

    }
}
