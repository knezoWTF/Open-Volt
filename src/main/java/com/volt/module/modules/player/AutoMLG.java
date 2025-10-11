package com.volt.module.modules.player;

import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.mc.InventoryUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public final class AutoMLG extends Module {
    private final NumberSetting fallDistance = new NumberSetting("Fall Distance", 3, 40, 8, 1);
    private final BooleanSetting pickUp = new BooleanSetting("Pick Up", true);
    private int stage;
    private int ticks;
    private int storedSlot = -1;
    private float storedPitch;
    private boolean changedPitch;

    public AutoMLG() {
        super("Auto MLG", "Places water before landing", -1, Category.PLAYER);
        this.addSettings(fallDistance, pickUp);
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (isNull()) return;
        if (stage == 0) {
            tryStart();
        } else if (stage == 1) {
            handlePlacement();
        } else if (stage == 2) {
            handlePickup();
        } else if (stage == 3) {
            finishSequence();
        }
    }

    private void tryStart() {
        if (mc.player.isOnGround()) return;
        if (mc.player.isTouchingWater()) return;
        if (mc.player.fallDistance < fallDistance.getValue()) return;
        if (mc.player.getVelocity().y >= -0.6) return;
        if (!InventoryUtil.hasItem(Items.WATER_BUCKET)) return;
        storedSlot = mc.player.getInventory().selectedSlot;
        storedPitch = mc.player.getPitch();
        InventoryUtil.swapToSlot(Items.WATER_BUCKET);
        stage = 1;
        ticks = 0;
        changedPitch = false;
    }

    private void handlePlacement() {
        if (!changedPitch) {
            mc.player.setPitch(89.5f);
            changedPitch = true;
            return;
        }
        if (mc.player.isOnGround()) {
            stage = 3;
            return;
        }
        int distance = findGroundDistance();
        ticks++;
        if (distance > 2) return;
        if (ticks % 2 != 0) return;
        InventoryUtil.swapToSlot(Items.WATER_BUCKET);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        stage = pickUp.getValue() ? 2 : 3;
        ticks = 0;
    }

    private void handlePickup() {
        ticks++;
        if (!mc.player.isOnGround() && !mc.player.isTouchingWater()) return;
        if (ticks < 3) return;
        InventoryUtil.swapToSlot(Items.BUCKET);
        ((MinecraftClientAccessor) mc).invokeDoItemUse();
        stage = 3;
    }

    private void finishSequence() {
        if (storedSlot >= 0) {
            mc.player.getInventory().selectedSlot = storedSlot;
        }
        if (changedPitch) {
            mc.player.setPitch(storedPitch);
        }
        stage = 0;
        ticks = 0;
        storedSlot = -1;
        changedPitch = false;
    }

    @Override
    public void onDisable() {
        finishSequence();
        super.onDisable();
    }

    private int findGroundDistance() {
        if (isNull()) return 6;
        BlockPos base = mc.player.getBlockPos();
        for (int i = 1; i <= 6; i++) {
            BlockPos check = base.down(i);
            if (!mc.world.getBlockState(check).isAir()) {
                return i;
            }
        }
        return 7;
    }
}
