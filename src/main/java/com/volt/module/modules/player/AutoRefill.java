package com.volt.module.modules.player;


import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.HandledScreenAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class AutoRefill extends Module {
    private static final NumberSetting refillDelay = new NumberSetting("Refill Delay", 50, 1000, 150, 25);
    private static final NumberSetting reactionDelayMin = new NumberSetting("Reaction Delay Min", 100, 1000, 200, 25);
    private static final NumberSetting reactionDelayMax = new NumberSetting("Reaction Delay Max", 200, 2000, 500, 25);
    private static final NumberSetting minStackCount = new NumberSetting("Min Stack Count", 1, 64, 16, 1);
    private static final BooleanSetting closeInventoryAfter = new BooleanSetting("Close Inventory After", true);
    private static final BooleanSetting onlyWhenSneaking = new BooleanSetting("Only When Sneaking", false);
    private static final BooleanSetting hoverMode = new BooleanSetting("Hover Mode", false);
    private static final NumberSetting hoverDelayMin = new NumberSetting("Hover Delay Min", 1, 1000, 100, 1);
    private static final NumberSetting hoverDelayMax = new NumberSetting("Hover Delay Max", 1, 1000, 300, 1);
    private static final BooleanSetting refillInstantHealth = new BooleanSetting("Refill Instant Health", true);
    private static final BooleanSetting refillRegeneration = new BooleanSetting("Refill Regeneration", true);
    private static final BooleanSetting refillStrength = new BooleanSetting("Refill Strength", false);
    private static final BooleanSetting refillSpeed = new BooleanSetting("Refill Speed", false);
    private static final int MAX_OPERATIONS_PER_TICK = 2;
    private final KeybindSetting refillKeybind = new KeybindSetting("Refill Key", GLFW.GLFW_KEY_R, false);
    private final TimerUtil refillTimer = new TimerUtil();
    private final TimerUtil reactionTimer = new TimerUtil();
    private final TimerUtil hoverTimer = new TimerUtil();
    private final Random random = new Random();
    private final SecureRandom secureRandom = new SecureRandom();
    private final Set<Item> healthPotions = new HashSet<>();
    private RefillState currentState = RefillState.IDLE;
    private boolean wasInventoryOpen = false;
    private int operationsThisTick = 0;
    private long reactionDelay = 0;

    private boolean keyPressed = false;

    public AutoRefill() {
        super("Auto Refill", "Automatically refills hotbar with health potions from inventory", -1, Category.PLAYER);
        this.addSettings(refillKeybind, refillDelay, reactionDelayMin, reactionDelayMax, minStackCount,
                closeInventoryAfter, onlyWhenSneaking, hoverMode, hoverDelayMin, hoverDelayMax,
                refillInstantHealth, refillRegeneration, refillStrength, refillSpeed);

        updateHealthPotionsSet();
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        operationsThisTick = 0;

        updateHealthPotionsSet();

        if (hoverDelayMin.getValueInt() >= hoverDelayMax.getValueInt()) {
            hoverDelayMax.setValue(hoverDelayMin.getValueInt() + 50);
        }

        if (hoverMode.getValue()) {
            handleHoverMode();
            return;
        }

        boolean currentKeyState = KeyUtils.isKeyPressed(refillKeybind.getKeyCode());

        if (currentKeyState && !keyPressed) {
            handleKeyPress();
        }

        keyPressed = currentKeyState;

        if (onlyWhenSneaking.getValue() && !mc.player.isSneaking()) {
            return;
        }

        switch (currentState) {
            case OPENING_INVENTORY:
                handleOpeningInventory();
                break;
            case REFILLING:
                handleRefilling();
                break;
            case CLOSING_INVENTORY:
                handleClosingInventory();
                break;
        }
    }

    private void updateHealthPotionsSet() {
        healthPotions.clear();

        if (refillInstantHealth.getValue()) {
            healthPotions.add(Items.POTION);
            healthPotions.add(Items.SPLASH_POTION);
            healthPotions.add(Items.LINGERING_POTION);
        }

        if (refillRegeneration.getValue()) {
            healthPotions.add(Items.POTION);
            healthPotions.add(Items.SPLASH_POTION);
            healthPotions.add(Items.LINGERING_POTION);
        }

        if (refillStrength.getValue()) {
            healthPotions.add(Items.POTION);
            healthPotions.add(Items.SPLASH_POTION);
            healthPotions.add(Items.LINGERING_POTION);
        }

        if (refillSpeed.getValue()) {
            healthPotions.add(Items.POTION);
            healthPotions.add(Items.SPLASH_POTION);
            healthPotions.add(Items.LINGERING_POTION);
        }
    }

    private void handleHoverMode() {
        if (!(mc.currentScreen instanceof InventoryScreen inv)) return;

        Slot focusedSlot;
        try {
            focusedSlot = ((HandledScreenAccessor) inv).getFocusedSlot();
        } catch (Exception e) {
            return;
        }

        if (focusedSlot == null) return;

        ItemStack focusedItemStack = focusedSlot.getStack();
        if (!isHealthPotion(focusedItemStack)) return;

        if (focusedSlot.getIndex() < 9) return;

        boolean hotbarNeedsRefill = false;
        for (int i = 0; i < 9; i++) {
            ItemStack hotbarStack = mc.player.getInventory().getStack(i);
            if (hotbarStack.isEmpty() || (isHealthPotion(hotbarStack) && hotbarStack.getCount() < minStackCount.getValueInt())) {
                hotbarNeedsRefill = true;
                break;
            }
        }

        if (!hotbarNeedsRefill) return;

        int minDelay = Math.max(1, hoverDelayMin.getValueInt());
        int maxDelay = Math.max(minDelay + 1, hoverDelayMax.getValueInt());

        long randomDelay;
        try {
            randomDelay = secureRandom.nextLong(minDelay, maxDelay);
        } catch (Exception e) {
            randomDelay = minDelay + random.nextInt(Math.max(1, maxDelay - minDelay));
        }

        if (hoverTimer.hasElapsedTime(randomDelay, true)) {
            try {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        focusedSlot.getIndex(),
                        0,
                        SlotActionType.QUICK_MOVE,
                        mc.player
                );
                hoverTimer.reset();
            } catch (Exception e) {
                hoverTimer.reset();
            }
        }
    }

    private void handleKeyPress() {
        if (currentState != RefillState.IDLE) {
            return;
        }

        boolean hasPotionsInInventory = false;
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isHealthPotion(stack)) {
                hasPotionsInInventory = true;
                break;
            }
        }

        if (hasPotionsInInventory) {
            startRefillProcess();
        }
    }

    private void startRefillProcess() {
        currentState = RefillState.OPENING_INVENTORY;
        wasInventoryOpen = mc.currentScreen instanceof InventoryScreen;
        refillTimer.reset();

        int minDelay = reactionDelayMin.getValueInt();
        int maxDelay = reactionDelayMax.getValueInt();
        reactionDelay = minDelay + random.nextInt(Math.max(1, maxDelay - minDelay + 1));
    }

    private void handleOpeningInventory() {
        if (mc.currentScreen instanceof InventoryScreen) {
            if (!reactionTimer.hasElapsedTime(reactionDelay)) {
                return;
            }
            currentState = RefillState.REFILLING;
        } else {
            assert mc.player != null;
            mc.setScreen(new InventoryScreen(mc.player));
            reactionTimer.reset();
        }
    }

    private void handleRefilling() {
        if (!(mc.currentScreen instanceof InventoryScreen)) {
            currentState = RefillState.IDLE;
            return;
        }

        if (!refillTimer.hasElapsedTime(refillDelay.getValueInt())) return;

        if (operationsThisTick >= MAX_OPERATIONS_PER_TICK) return;

        int targetHotbarSlot = findSlotNeedingRefill();
        if (targetHotbarSlot == -1) {
            finishRefilling();
            return;
        }

        int sourceSlot = findHealthPotionInInventory();
        if (sourceSlot == -1) {
            finishRefilling();
            return;
        }

        moveItemToHotbar(sourceSlot);
        operationsThisTick++;

        refillTimer.reset();

        try {
            Thread.sleep(10 + random.nextInt(30));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void finishRefilling() {
        if (closeInventoryAfter.getValue() && !wasInventoryOpen) {
            currentState = RefillState.CLOSING_INVENTORY;
            refillTimer.reset();
        } else {
            currentState = RefillState.IDLE;
        }
    }

    private void handleClosingInventory() {
        if (!refillTimer.hasElapsedTime(refillDelay.getValueInt())) return;

        if (mc.currentScreen instanceof InventoryScreen) {
            mc.setScreen(null);
        }
        currentState = RefillState.IDLE;
    }

    private int findSlotNeedingRefill() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isHealthPotion(stack) && stack.getCount() < minStackCount.getValueInt()) {
                return i;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    private int findHealthPotionInInventory() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isHealthPotion(stack)) {
                return i;
            }
        }
        return -1;
    }

    private void moveItemToHotbar(int sourceSlot) {
        if (mc.interactionManager == null || !(mc.currentScreen instanceof InventoryScreen inventoryScreen)) return;

        mc.interactionManager.clickSlot(
                inventoryScreen.getScreenHandler().syncId,
                sourceSlot,
                0,
                SlotActionType.QUICK_MOVE,
                mc.player
        );
    }

    private boolean isHealthPotion(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        if (!healthPotions.contains(item)) return false;

        String itemName = stack.getName().getString().toLowerCase();

        if (refillInstantHealth.getValue() &&
                (itemName.contains("healing") || itemName.contains("health"))) {
            return true;
        }

        if (refillRegeneration.getValue() && itemName.contains("regeneration")) {
            return true;
        }

        if (refillStrength.getValue() && itemName.contains("strength")) {
            return true;
        }

        return refillSpeed.getValue() && itemName.contains("speed");
    }

    @Override
    public void onEnable() {
        refillTimer.reset();
        reactionTimer.reset();
        hoverTimer.reset();
        currentState = RefillState.IDLE;

        keyPressed = KeyUtils.isKeyPressed(refillKeybind.getKeyCode());

        updateHealthPotionsSet();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        currentState = RefillState.IDLE;
        hoverTimer.reset();

        if (mc.currentScreen instanceof InventoryScreen && !wasInventoryOpen) {
            mc.setScreen(null);
        }

        super.onDisable();
    }

    private enum RefillState {
        IDLE,
        OPENING_INVENTORY,
        REFILLING,
        CLOSING_INVENTORY
    }


}