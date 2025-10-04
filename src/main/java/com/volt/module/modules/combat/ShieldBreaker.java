package com.volt.module.modules.combat;

import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.CombatUtil;
import com.volt.utils.mc.InventoryUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;

public final class ShieldBreaker extends Module {
    public static boolean breakingShield = false;
    private final NumberSetting reactionDelay = new NumberSetting("Reaction Delay", 0, 250, 50, 5);
    private final NumberSetting swapDelay = new NumberSetting("Swap Delay", 0, 500, 100, 10);
    private final NumberSetting attackDelay = new NumberSetting("Attack Delay", 0, 500, 100, 10);
    private final NumberSetting swapBackDelay = new NumberSetting("Swap Back Delay", 0, 500, 150, 10);
    private final BooleanSetting revertSlot = new BooleanSetting("Revert Slot", true);
    private final BooleanSetting rayTraceCheck = new BooleanSetting("Check Facing", true);
    private final BooleanSetting disableIfUsingItem = new BooleanSetting("Disable if using item", true);
    private final TimerUtil reactionTimer = new TimerUtil();
    private final TimerUtil swapTimer = new TimerUtil();
    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil swapBackTimer = new TimerUtil();
    private int savedSlot = -1;

    public ShieldBreaker() {
        super("Shield Breaker", "Automatically breaks the opponents shield", -1, Category.COMBAT);
        this.addSettings(reactionDelay, swapDelay, attackDelay, swapBackDelay, revertSlot, rayTraceCheck, disableIfUsingItem);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || mc.currentScreen != null) return;
        if (!InventoryUtil.hasWeapon(AxeItem.class)) return;
        if (mc.player.isUsingItem() && disableIfUsingItem.getValue()) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof PlayerEntity target)) return;

        boolean isBlocking = target.isBlocking() && target.isHolding(Items.SHIELD);
        boolean canBreak = !rayTraceCheck.getValue() || !CombatUtil.isShieldFacingAway(target);

        if (isBlocking && canBreak) {
            if (!(mc.player.getMainHandStack().getItem() instanceof AxeItem)) {
                if (reactionTimer.hasElapsedTime(reactionDelay.getValueInt())) {
                    if (savedSlot == -1 && swapTimer.hasElapsedTime(swapDelay.getValueInt())) {
                        breakingShield = true;
                        savedSlot = mc.player.getInventory().selectedSlot;
                        InventoryUtil.swapToWeapon(AxeItem.class);
                        attackTimer.reset();
                    }
                }
            }
            if (mc.player.getMainHandStack().getItem() instanceof AxeItem && attackTimer.hasElapsedTime(attackDelay.getValueInt())) {
                ((MinecraftClientAccessor) mc).invokeDoAttack();
                attackTimer.reset();
                swapBackTimer.reset();
                breakingShield = false;
            }
        } else {
            reactionTimer.reset();
            if (savedSlot != -1 && swapBackTimer.hasElapsedTime(swapBackDelay.getValueInt())) {
                if (revertSlot.getValue()) {
                    mc.player.getInventory().selectedSlot = savedSlot;
                }
                savedSlot = -1;
            }
        }
    }

    @Override
    public void onDisable() {
        if (savedSlot != -1 && revertSlot.getValue()) {
            mc.player.getInventory().selectedSlot = savedSlot;
        }
        savedSlot = -1;
        breakingShield = false;
        super.onDisable();
    }
}

