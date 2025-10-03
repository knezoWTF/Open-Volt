package com.volt.module.modules.combat;

import com.volt.event.impl.player.EventAttack;
import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.mc.EnchantmentUtil;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import com.volt.module.setting.BooleanSetting;
public final class BreachSwap extends Module {

    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 10, 100, 30, 1);
    private final BooleanSetting onlyOnGround = new BooleanSetting("Only on ground", true);

    private int originalSlot = -1;
    private boolean shouldSwitchBack = false;
    private long switchTime = 0;
    private boolean isSwappingAttack = false;

    public BreachSwap() {
        super("Breach Swap", "Switches to a Breach enchanted mace when attacking", Category.COMBAT);
        addSettings(switchDelay, onlyOnGround);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (isNull() || isSwappingAttack) return;
        if (onlyOnGround.getValue() && !mc.player.isOnGround()) return;
        if (ShieldBreaker.breakingShield) return;
        if (!(event.getTarget() instanceof LivingEntity)) return;

        int maceSlot = findBreachMaceSlot();
        if (maceSlot == -1) return;

        if (originalSlot == -1) {
            originalSlot = mc.player.getInventory().selectedSlot;
        }

        mc.player.getInventory().selectedSlot = maceSlot;

        isSwappingAttack = true;
        ((MinecraftClientAccessor) mc).invokeDoAttack();
        isSwappingAttack = false;

        shouldSwitchBack = true;
        switchTime = System.currentTimeMillis();
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

        if (mc.options.attackKey.isPressed()) {
            HitResult hitResult = mc.crosshairTarget;
            if (hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity) {
                int maceSlot = findBreachMaceSlot();
                if (maceSlot != -1) {
                    originalSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = maceSlot;

                    ((MinecraftClientAccessor) mc).invokeDoAttack();

                    switchTime = System.currentTimeMillis();
                    shouldSwitchBack = true;
                }
            }
        }
    }

    private int findBreachMaceSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            Item item = stack.getItem();
            if (item instanceof MaceItem && hasBreach(stack)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasBreach(ItemStack stack) {
        return EnchantmentUtil.hasEnchantment(stack, mc.world, Enchantments.BREACH);
    }

    public boolean isSwapping() {
        return shouldSwitchBack || isSwappingAttack;
    }

    @Override
    public void onDisable() {
        if (originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
            originalSlot = -1;
        }
        shouldSwitchBack = false;
        isSwappingAttack = false;
    }
}
