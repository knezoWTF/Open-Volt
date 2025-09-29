package com.volt.module.modules.player;

import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class AutoDoubleHand extends Module {
    
    private static final BooleanSetting inventorySwitch = new BooleanSetting("Inventory Switch", true);
    private static final NumberSetting totemSlot = new NumberSetting("Totem Slot", 1, 9, 9, 1);
    private static final BooleanSetting heightSwitch = new BooleanSetting("Height Switch", true);
    private static final NumberSetting heightThreshold = new NumberSetting("Height Threshold", 1.0, 10.0, 3.0, 0.1);
    private static final BooleanSetting healthSwitch = new BooleanSetting("Health Switch", true);
    private static final NumberSetting healthThreshold = new NumberSetting("Health Threshold", 1.0, 20.0, 6.0, 0.5);
    
    private int originalSlot = -1;
    private final TimerUtil groundLevelTimer = new TimerUtil();
    private double cachedGroundLevel = 0;
    private static final long GROUND_LEVEL_CACHE_MS = 500;
    
    public AutoDoubleHand() {
        super("Auto Double Hand", "Automatically switches to totem based on conditions", -1, Category.PLAYER);
        this.addSettings(inventorySwitch, totemSlot, heightSwitch, heightThreshold, healthSwitch, healthThreshold);
    }
    
    @EventHandler
    private void onTick(TickEvent event) {
        if (isNull()) return;
        
        boolean needsTotem = shouldHoldTotem();
        boolean hasTotem = isHoldingTotem();
        
        if (needsTotem && !hasTotem) {
            switchToTotem();
        } else if (!needsTotem && hasTotem && originalSlot != -1) {
            switchBack();
        }
    }
    
    private boolean shouldHoldTotem() {
        if (isNull()) return false;

        if (mc.currentScreen != null && !inventorySwitch.getValue()) return false;
        
        if (inventorySwitch.getValue() && mc.currentScreen instanceof InventoryScreen) {
            return true;
        }
        
        if (heightSwitch.getValue()) {
            double playerY = mc.player.getY();
            double groundY = getCachedGroundLevel();
            if ((playerY - groundY) > heightThreshold.getValue()) {
                return true;
            }
        }
        
        if (healthSwitch.getValue()) {
            if (mc.player.getHealth() <= healthThreshold.getValue()) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean isHoldingTotem() {
        if (isNull()) return false;
        
        int currentSlot = mc.player.getInventory().selectedSlot;
        
        if (originalSlot != -1 && currentSlot == (totemSlot.getValueInt() - 1)) {
            return true;
        }
        
        ItemStack heldItem = mc.player.getInventory().getStack(currentSlot);
        return !heldItem.isEmpty() && heldItem.getItem() == Items.TOTEM_OF_UNDYING;
    }
    
    private void switchToTotem() {
        if (isNull()) return;
        
        int totemSlotIndex = findTotemInHotbar();
        
        if (totemSlotIndex == -1 && inventorySwitch.getValue() && mc.currentScreen instanceof InventoryScreen) {
            totemSlotIndex = totemSlot.getValueInt() - 1;
        }
        
        if (totemSlotIndex != -1) {
            originalSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = totemSlotIndex;
        }
    }
    
    private void switchBack() {
        if (isNull()) return;
        
        mc.player.getInventory().selectedSlot = originalSlot;
        originalSlot = -1;
    }
    
    private int findTotemInHotbar() {
        if (isNull()) return -1;
        
        for (int i = 0; i < 9; i++) {
            if (i >= 0 && i < 9) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private double getCachedGroundLevel() {
        if (isNull()) return 0;
        
        if (groundLevelTimer.hasElapsedTime(GROUND_LEVEL_CACHE_MS, true)) {
            cachedGroundLevel = calculateGroundLevel();
        }
        
        return cachedGroundLevel;
    }
    
    private double calculateGroundLevel() {
        if (isNull()) return 0;
        
        double playerY = mc.player.getY();
        
        for (int y = (int) playerY; y >= mc.world.getBottomY(); y--) {
            if (!mc.world.getBlockState(mc.player.getBlockPos().withY(y)).isAir()) {
                return y + 1;
            }
        }
        
        return mc.world.getBottomY();
    }
    
    @Override
    public void onDisable() {
        if (!isNull() && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
            originalSlot = -1;
        }
        groundLevelTimer.reset();
        super.onDisable();
    }
    
    @Override
    public void onEnable() {
        groundLevelTimer.reset();
        super.onEnable();
    }
}