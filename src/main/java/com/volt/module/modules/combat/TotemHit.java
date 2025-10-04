package com.volt.module.modules.combat;

import com.volt.event.impl.player.TickEvent;
import meteordevelopment.orbit.EventHandler;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.NumberSetting;

import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TotemHit extends Module {
    
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 10, 100, 30, 1);
    
    private int originalSlot = -1;
    private boolean shouldSwitchBack = false;
    private long switchTime = 0;
    private boolean attackPressedLastTick = false;
    
    public TotemHit() {
        super("Totem Hit", "Switches to sword when attacking with totem", Category.COMBAT);
        addSettings(switchDelay);
    }
    
    @EventHandler
    public void onTick(TickEvent event) {
        if (isNull()) return;
        
        if (shouldSwitchBack && System.currentTimeMillis() - switchTime >= switchDelay.getValue()) {
            if (originalSlot != -1) {
                mc.player.getInventory().selectedSlot = originalSlot;
                originalSlot = -1;
            }
            shouldSwitchBack = false;
        }
        
        boolean attackPressed = mc.options.attackKey.isPressed();
        if (attackPressed && !attackPressedLastTick && mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING) {
            HitResult hitResult = mc.crosshairTarget;
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) hitResult).getEntity();
                if (target != null) {
                    int swordSlot = findSwordSlot();
                    if (swordSlot != -1) {
                        originalSlot = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = swordSlot;
                        ((MinecraftClientAccessor) mc).invokeDoAttack();
                        switchTime = System.currentTimeMillis();
                        shouldSwitchBack = true;
                    }
                }
            }
        }

        attackPressedLastTick = attackPressed;
    }
    
    private int findSwordSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof SwordItem) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public void onDisable() {
        if (originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
            originalSlot = -1;
        }
        shouldSwitchBack = false;
        attackPressedLastTick = false;
    }
}