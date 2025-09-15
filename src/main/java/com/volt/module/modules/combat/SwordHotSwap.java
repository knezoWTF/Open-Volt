package com.volt.module.modules.combat;

import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import org.lwjgl.glfw.GLFW;

public final class SwordHotSwap extends Module {

    private final KeybindSetting hotswapKey = new KeybindSetting("Hotswap Key", GLFW.GLFW_MOUSE_BUTTON_2, false);
    private final NumberSetting swapDelay = new NumberSetting("Swap Delay (MS)", 0, 1000, 150, 25);
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", true);

    private final TimerUtil swapTimer = new TimerUtil();
    private boolean isSwapping = false;
    private boolean keyPressed = false;
    private int originalSlot = -1;

    public SwordHotSwap() {
        super("Sword Hotswap", "Swaps to shield only if holding a sword when RMB is pressed", -1, Category.COMBAT);
        this.addSettings(hotswapKey, swapDelay, switchBack);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(hotswapKey));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || mc.player == null) return;

        boolean currentKeyState = KeyUtils.isKeyPressed(hotswapKey.getKeyCode());

        if (currentKeyState && !keyPressed) {
            startSwap();
        }

        if (!currentKeyState && keyPressed && switchBack.getValue() && originalSlot != -1) {
            if (mc.player.getInventory().selectedSlot == findShieldInHotbar()) {
                mc.player.getInventory().selectedSlot = originalSlot;
            }
            originalSlot = -1;
            isSwapping = false;
        }

        keyPressed = currentKeyState;

        if (isSwapping && swapTimer.hasElapsedTime(swapDelay.getValueInt())) {
            performSwap();
            isSwapping = false;
        }
    }

    private void startSwap() {
        ItemStack mainHand = mc.player.getMainHandStack();
        if (!isSword(mainHand)) return;

        int shieldSlot = findShieldInHotbar();
        if (shieldSlot == -1) return;

        originalSlot = mc.player.getInventory().selectedSlot;
        isSwapping = true;
        swapTimer.reset();
    }

    private void performSwap() {
        int shieldSlot = findShieldInHotbar();
        if (shieldSlot != -1) {
            mc.player.getInventory().selectedSlot = shieldSlot;
        }
    }

    private int findShieldInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.SHIELD) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSword(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public int getKey() {
        return -1;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        isSwapping = false;
        keyPressed = false;
        originalSlot = -1;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        isSwapping = false;
        keyPressed = false;
        originalSlot = -1;
    }
}
