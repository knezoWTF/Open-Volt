package com.volt.module.modules.player;


import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public final class FastEXP extends Module {

    private static final NumberSetting chance = new NumberSetting("Chance %", 1, 100, 75, 1);

    public FastEXP() {
        super("Fast Exp", "Bypasses item use cooldown for faster experience bottle throwing", -1, Category.PLAYER);
        this.addSettings(chance);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;

        ItemStack heldItem = mc.player.getMainHandStack();
        if (heldItem.isEmpty() || heldItem.getItem() != Items.EXPERIENCE_BOTTLE) return;

        if (!KeyUtils.isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_2)) {
            return;
        }

        if (chance.getValue() >= Math.random() * 100) {
            ((MinecraftClientAccessor) mc).invokeDoItemUse();
        }
    }
} 