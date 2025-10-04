package com.volt.module.modules.misc;


import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public final class CartKey extends Module {
    private final KeybindSetting key = new KeybindSetting("Key", GLFW.GLFW_KEY_C, true);
    private final NumberSetting delay = new NumberSetting("Delay", 50, 500, 150, 25);
    private final BooleanSetting silent = new BooleanSetting("Silent", true);
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", true);
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 100, 1000, 250, 50);
    private final BooleanSetting bow = new BooleanSetting("Auto Bow", true);
    private final NumberSetting bowWindow = new NumberSetting("Bow Window", 500, 2000, 1000, 100);

    private boolean pressed, charging;
    private int originalSlot = -1;
    private long lastAction, bowStart;
    private State state = State.IDLE;

    public CartKey() {
        super("Cart Key", "Places rail + TNT cart, auto switches to bow", -1, Category.MISC);
        addSettings(key, delay, silent, switchBack, switchDelay, bow, bowWindow);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || mc.currentScreen != null) return;

        boolean keyDown = KeyUtils.isKeyPressed(key.getKeyCode());
        boolean rightClick = mc.options.useKey.isPressed();
        long now = System.currentTimeMillis();

        if (keyDown && !pressed && state == State.IDLE && canPlace()) {
            originalSlot = mc.player.getInventory().selectedSlot;
            state = State.RAIL;
            lastAction = now;
        }
        pressed = keyDown;

        switch (state) {
            case IDLE -> {
            }
            case RAIL -> {
                if (now - lastAction >= delay.getValueInt()) placeRail(now);
            }
            case CART -> {
                if (now - lastAction >= delay.getValueInt()) placeCart(now);
            }
            case SWITCH -> {
                if (now - lastAction >= switchDelay.getValueInt()) switchSlot();
            }
            case BOW_WAIT -> {
                if (rightClick) activateBow();
                else if (now - bowStart >= bowWindow.getValueInt()) reset();
            }
            case BOW_CHARGE -> {
                if (!rightClick && charging) finishBow();
            }
        }
    }

    private boolean canPlace() {
        if (!(mc.crosshairTarget instanceof BlockHitResult hit)) return false;
        BlockPos pos = hit.getBlockPos().offset(hit.getSide());
        return mc.world != null && mc.player != null &&
                mc.player.getPos().distanceTo(pos.toCenterPos()) <= 4.5 &&
                mc.world.getBlockState(pos).isAir() &&
                hasItem(Items.RAIL) && hasItem(Items.TNT_MINECART);
    }

    private void placeRail(long now) {
        if (useItem(Items.RAIL)) {
            state = State.CART;
            lastAction = now;
        } else reset();
    }

    private void placeCart(long now) {
        if (useItem(Items.TNT_MINECART)) {
            if (bow.getValue()) {
                state = State.BOW_WAIT;
                bowStart = now;
            } else if (switchBack.getValue() && originalSlot != -1) {
                state = State.SWITCH;
                lastAction = now;
            } else reset();
        } else reset();
    }

    private void switchSlot() {
        if (originalSlot != -1) mc.player.getInventory().selectedSlot = originalSlot;
        reset();
    }

    private void activateBow() {
        int bowSlot = findBow();
        if (bowSlot != -1) {
            mc.player.getInventory().selectedSlot = bowSlot;
            state = State.BOW_CHARGE;
            charging = true;
        } else reset();
    }

    private void finishBow() {
        charging = false;
        if (switchBack.getValue() && originalSlot != -1) {
            int slotToRestore = originalSlot;
            reset();
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                if (mc.player != null) mc.player.getInventory().selectedSlot = slotToRestore;
            }).start();
        } else {
            reset();
        }
    }

    private boolean hasItem(net.minecraft.item.Item item) {
        return findItem(item) != -1;
    }

    private boolean useItem(net.minecraft.item.Item item) {
        int slot = findItem(item);
        if (slot == -1) return false;

        if (silent.getValue()) {
            int current = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            ((MinecraftClientAccessor) mc).invokeDoItemUse();
            mc.player.getInventory().selectedSlot = current;
        } else {
            mc.player.getInventory().selectedSlot = slot;
            ((MinecraftClientAccessor) mc).invokeDoItemUse();
        }
        return true;
    }

    private int findItem(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) return i;
        }
        return -1;
    }

    private int findBow() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BowItem) return i;
        }
        return -1;
    }

    private void reset() {
        state = State.IDLE;
        originalSlot = -1;
        charging = false;
    }

    @Override
    public void onEnable() {
        pressed = false;
        reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }

    private enum State {IDLE, RAIL, CART, SWITCH, BOW_WAIT, BOW_CHARGE}
}
