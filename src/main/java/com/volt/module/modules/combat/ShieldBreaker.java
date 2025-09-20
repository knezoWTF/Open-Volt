package com.volt.module.modules.combat;

import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.mc.CombatUtil;
import com.volt.utils.mc.InventoryUtil;
import com.volt.utils.mc.MouseSimulation;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class ShieldBreaker extends Module {
    private final NumberSetting hitDelay = new NumberSetting("Hit Delay", 1, 20, 0, 1);
    private final NumberSetting slotDelay = new NumberSetting("Slot Delay", 1, 20, 0, 1);
    private final NumberSetting cpsLimit = new NumberSetting("CPS", 1, 15, 3, 1);

    private final BooleanSetting revertSlot = new BooleanSetting("Go back to original slot", true);
    private final BooleanSetting autoStun = new BooleanSetting("Auto Stun", false);
    private final BooleanSetting requireAxe = new BooleanSetting("Require Axe", false);
    private final BooleanSetting rayTraceCheck = new BooleanSetting("Check Facing", true);
    private final BooleanSetting requireClick = new BooleanSetting("Require Click", false);
	private final BooleanSetting ignoreIfUsingItem = new BooleanSetting("Ignore if using item", true);

    private final TimerUtil hitDelayTimer = new TimerUtil();
    private final TimerUtil slotTimer = new TimerUtil();
    public boolean breakingShieldFuckNigga = false;
    private int savedSlot = -1;

    public ShieldBreaker() {
        super("Shield Breaker", "Automatically breaks the opponents shield", -1, Category.COMBAT);
        this.addSettings(hitDelay, slotDelay, cpsLimit, revertSlot, autoStun, requireAxe, rayTraceCheck, requireClick, ignoreIfUsingItem);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;
        if (requireAxe.getValue() && !(mc.player.getMainHandStack().getItem() instanceof AxeItem)) return;
        if (requireClick.getValue() && !mc.mouse.wasLeftButtonClicked()) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult entityHit)) return;

        var entity = entityHit.getEntity();
        if (!(entity instanceof PlayerEntity player)) return;
		if (ignoreIfUsingItem.getValue() && mc.player.isUsingItem()) return;
        if (rayTraceCheck.getValue() && CombatUtil.isShieldFacingAway((LivingEntity) entity)) return;
        if (!player.isHolding(Items.SHIELD) || !player.isBlocking()) {
            if (savedSlot != -1) {
                if (revertSlot.getValue()) mc.player.getInventory().selectedSlot = savedSlot;
                savedSlot = -1;
            }
            return;
        }

        if (savedSlot == -1) savedSlot = mc.player.getInventory().selectedSlot;
        if (!slotTimer.hasElapsedTime(slotDelay.getValueInt() * 50)) return;
        breakingShieldFuckNigga = true;
        InventoryUtil.swapToWeapon(AxeItem.class);

        int minClickDelay = 1000 / cpsLimit.getValueInt();
        if (!hitDelayTimer.hasElapsedTime(minClickDelay)) return;

        if (!hitDelayTimer.hasElapsedTime(hitDelay.getValueInt() * 50)) return;

        ((MinecraftClientAccessor) mc).invokeDoAttack();
        MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);

        if (autoStun.getValue()) {
            ((MinecraftClientAccessor) mc).invokeDoAttack();
            MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }

        hitDelayTimer.reset();
        slotTimer.reset();
        breakingShieldFuckNigga = false;
    }

    @Override
    public void onDisable() {
        if (savedSlot != -1 && revertSlot.getValue()) {
            mc.player.getInventory().selectedSlot = savedSlot;
        }
        savedSlot = -1;
        breakingShieldFuckNigga = false;
        super.onDisable();
    }
}