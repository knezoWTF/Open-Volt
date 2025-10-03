package com.volt.module.modules.player;


import org.lwjgl.glfw.GLFW;

import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.MathUtils;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.InventoryUtil;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;

public final class AutoExtinguish extends Module {
    public static final NumberSetting maxMS = new NumberSetting("Max MS", 0, 1200, 250, 0.5);
    public static final NumberSetting minMS = new NumberSetting("Min MS", 0, 1200, 330, 0.5);
    public static final BooleanSetting pickUP = new BooleanSetting("Pick up after", false);
    public static final BooleanSetting rotateBack = new BooleanSetting("Rotate back", false);
    public static final BooleanSetting toPrevSlot = new BooleanSetting("Goto prev-slot", false);

    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil delay = new TimerUtil();
    private boolean isExtinguishing = false;
    private float originalPitch;
    private int originalSlot;
    private long currentDelay;
    private State currentState = State.READY;

    public AutoExtinguish() {
        super("Auto Extinguish", "Automatically places water to extinguish you", -1, Category.PLAYER);
        this.addSettings(maxMS, minMS, pickUP, rotateBack, toPrevSlot);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (minMS.getValueFloat() > maxMS.getValueFloat()) {
            minMS.setValue(maxMS.getValue() - 1);
        }
        if (minMS.getValue() == maxMS.getValue()) minMS.setValue(maxMS.getValue());

        if (mc.player.isOnFire()) {
            if (!isExtinguishing) {
                startExtinguishing();
            } else {
                handleExtinguishing();
            }
        } else if (isExtinguishing) {
            finishExtinguishing();
        }
    }

    ;

    private void startExtinguishing() {
        isExtinguishing = true;
        originalPitch = mc.player.getPitch();
        originalSlot = mc.player.getInventory().selectedSlot;

        long min = minMS.getValueInt();
        long max = maxMS.getValueInt();

        if (min >= max) {
            currentDelay = min;
        } else {
            currentDelay = (long) MathUtils.randomDoubleBetween(min, max);
        }

        currentState = State.SWITCHING;
        timer.reset();
        delay.reset();
    }

    private void handleExtinguishing() {
        if (timer.hasElapsedTime(currentDelay)) {
            switch (currentState) {
                case SWITCHING:
                    InventoryUtil.swapToSlot(Items.WATER_BUCKET);
                    currentState = State.AIMING;
                    timer.reset();
                    break;

                case AIMING:
                    mc.player.setPitch(89.9f);
                    currentState = State.PLACING;
                    timer.reset();
                    break;

                case PLACING:
                    InventoryUtil.swapToSlot(Items.WATER_BUCKET);
                    ((MinecraftClientAccessor) mc).invokeDoItemUse();
                    currentState = pickUP.getValue() ? State.PICKING_UP : State.RETURNING;
                    timer.reset();
                    break;

                case PICKING_UP:
                    if (delay.hasElapsedTime(100)) {
                        ((MinecraftClientAccessor) mc).invokeDoItemUse();
                        currentState = State.RETURNING;
                        timer.reset();
                    }
                    break;

                case RETURNING:
                    if (toPrevSlot.getValue()) {
                        mc.player.getInventory().selectedSlot = originalSlot;
                    }
                    if (rotateBack.getValue()) {
                        mc.player.setPitch(originalPitch);
                    }
                    isExtinguishing = false;
                    currentState = State.READY;
                    this.toggle();
                    break;
            }
        }
    }

    private void finishExtinguishing() {
        isExtinguishing = false;
        if (toPrevSlot.getValue()) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }
        if (rotateBack.getValue()) {
            mc.player.setPitch(originalPitch);
        }
        currentState = State.READY;
    }

    @Override
    public void onEnable() {
        delay.reset();
        timer.reset();
        isExtinguishing = false;
        currentState = State.READY;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        delay.reset();
        timer.reset();
        if (isExtinguishing) {
            finishExtinguishing();
        }
        super.onDisable();
    }

    private enum State {
        READY,
        SWITCHING,
        AIMING,
        PLACING,
        PICKING_UP,
        RETURNING
    }
}