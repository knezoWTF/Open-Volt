package com.volt.module.modules.misc;


import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.HandledScreenAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Random;

public final class HoverTotem extends Module {
    public static final NumberSetting minMS = new NumberSetting("Min MS", 1, 1000, 200, 0.5);
    public static final NumberSetting maxMS = new NumberSetting("Max MS", 1, 1000, 400, 0.5);
    public static final BooleanSetting ignoreOffhand = new BooleanSetting("Ignore Offhand", false);

    private final TimerUtil timer = new TimerUtil();
    private final Random random = new Random();

    public HoverTotem() {
        super("Hover Totem", "Puts a totem in your offhand and hotbar once hovered", -1, Category.MISC);
        this.addSettings(minMS, maxMS, ignoreOffhand);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (minMS.getValueFloat() >= maxMS.getValueFloat()) {
            minMS.setValue(maxMS.getValueFloat() - 0.5);
        }

        totemHandler();
    }

    private void totemHandler() {
        if (isNull()) return;
        if (!(mc.currentScreen instanceof InventoryScreen inv)) return;

        Slot focusedSlot = ((HandledScreenAccessor) inv).getFocusedSlot();
        if (focusedSlot == null) return;

        ItemStack focusedItemStack = focusedSlot.getStack();
        if (focusedItemStack.getItem() != Items.TOTEM_OF_UNDYING) return;

        if (focusedSlot.getIndex() < 9) return;

        ItemStack offhandItem = mc.player.getOffHandStack();
        boolean needsOffhand = offhandItem.isEmpty() || (offhandItem.getItem() != Items.TOTEM_OF_UNDYING && !ignoreOffhand.getValue());
        boolean needsHotbar = !hasTotemInHotbar();

        if (!needsOffhand && !needsHotbar) return;

        int minDelay = minMS.getValueInt();
        int maxDelay = maxMS.getValueInt();

        if (minDelay >= maxDelay) {
            maxDelay = minDelay + 1;
        }

        long delay = random.nextLong(minDelay, maxDelay);
        if (timer.hasElapsedTime(delay, true)) {
            assert mc.player != null;
            assert mc.interactionManager != null;

            if (needsOffhand) {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        focusedSlot.getIndex(),
                        40,
                        SlotActionType.SWAP,
                        mc.player
                );
            } else {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        focusedSlot.getIndex(),
                        0,
                        SlotActionType.QUICK_MOVE,
                        mc.player
                );
            }
        }
    }

    private boolean hasTotemInHotbar() {
        if (isNull()) return false;
        return mc.player.getInventory().main.subList(0, 9).stream()
                .anyMatch(stack -> stack.getItem() == Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void onEnable() {
        timer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        timer.reset();
        super.onDisable();
    }
}
