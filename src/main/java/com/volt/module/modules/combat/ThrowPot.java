package com.volt.module.modules.combat;


import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.KeybindSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import org.lwjgl.glfw.GLFW;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public final class ThrowPot extends Module {
    private static final KeybindSetting throwKey = new KeybindSetting("Throw Key", GLFW.GLFW_KEY_G, false);
    private static final NumberSetting throwDelay = new NumberSetting("Throw Delay", 50, 1000, 250, 50);
    private static final NumberSetting healthThreshold = new NumberSetting("Health Threshold", 1, 20, 10, 0.5);
    private static final BooleanSetting multiThrow = new BooleanSetting("Multi Throw", true);
    private static final NumberSetting lowHealthThreshold = new NumberSetting("Low Health Threshold", 1, 10, 4, 0.5);
    private static final NumberSetting lowHealthPots = new NumberSetting("Low Health Pots", 1, 5, 2, 1);
    private static final NumberSetting criticalHealthThreshold = new NumberSetting("Critical Health Threshold", 1, 6, 2, 0.5);
    private static final NumberSetting criticalHealthPots = new NumberSetting("Critical Health Pots", 1, 5, 3, 1);
    private static final NumberSetting potDelay = new NumberSetting("Pot Delay", 50, 500, 150, 25);
    private static final BooleanSetting autoSwitch = new BooleanSetting("Auto Switch Back", true);
    private static final BooleanSetting randomize = new BooleanSetting("Randomize Selection", false);
    private static final BooleanSetting lookDown = new BooleanSetting("Look Down", true);
    private static final BooleanSetting rotateBack = new BooleanSetting("Rotate Back", true);
    private static final NumberSetting rotationSpeed = new NumberSetting("Rotation Speed", 1, 90, 45, 1);

    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil potTimer = new TimerUtil();
    private final SecureRandom random = new SecureRandom();
    private final List<Integer> potionSlots = new ArrayList<>();
    private int currentPotionIndex = 0;
    private int originalSlot = -1;
    private float originalPitch = 0;
    private float targetPitch = 0;
    private boolean keyPressed = false;
    private boolean isThrowing = false;
    private boolean hasRotated = false;
    private boolean isRotating = false;
    private boolean isRotatingBack = false;
    private boolean readyToThrow = false;
    private int potsToThrow = 0;
    private int potsThrown = 0;
    private boolean isMultiThrowing = false;

    public ThrowPot() {
        super("Throw Pot", "Throws instant health potions based on health levels", -1, Category.COMBAT);
        this.addSettings(throwKey, throwDelay, healthThreshold, multiThrow, lowHealthThreshold, lowHealthPots,
                criticalHealthThreshold, criticalHealthPots, potDelay, autoSwitch, randomize,
                lookDown, rotateBack,  rotationSpeed);
        this.getSettings().removeIf(setting -> setting instanceof KeybindSetting && !setting.equals(throwKey));
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        boolean currentKeyState = KeyUtils.isKeyPressed(throwKey.getKeyCode());

        if (currentKeyState && !keyPressed && !isThrowing && !isRotating && !isRotatingBack && !isMultiThrowing) {
            if (timer.hasElapsedTime(throwDelay.getValueInt()) && isHealthBelowThreshold()) {
                startThrowProcess();
                timer.reset();
            }
        }

        keyPressed = currentKeyState;

        if (readyToThrow && !isRotating) {
            executeThrow();
        }

        if (isMultiThrowing && potTimer.hasElapsedTime(potDelay.getValueInt()) && potsThrown < potsToThrow) {
            throwNextPotion();
        }

        if (isThrowing && !isRotatingBack && !isMultiThrowing && timer.hasElapsedTime(100)) {
            finishThrow();
        }

        if (isMultiThrowing && potsThrown >= potsToThrow && timer.hasElapsedTime(100)) {
            finishMultiThrow();
        }
        if (mc.player == null || mc.world == null) {
            return;
        }

        handleRotation();
    }

    ;

    private void handleRotation() {
        if (isRotating) {
            float currentPitch = mc.player.getPitch();
            float difference = targetPitch - currentPitch;

            if (Math.abs(difference) < 1.0f) {
                mc.player.setPitch(targetPitch);
                isRotating = false;
            } else {
                float step = Math.signum(difference) * Math.min(Math.abs(difference), rotationSpeed.getValueFloat());
                mc.player.setPitch(currentPitch + step);
            }
        }

        if (isRotatingBack) {
            float currentPitch = mc.player.getPitch();
            float difference = originalPitch - currentPitch;

            if (Math.abs(difference) < 1.0f) {
                mc.player.setPitch(originalPitch);
                isRotatingBack = false;
                hasRotated = false;
            } else {
                float step = Math.signum(difference) * Math.min(Math.abs(difference), rotationSpeed.getValueFloat());
                mc.player.setPitch(currentPitch + step);
            }
        }
    }

    private void startThrowProcess() {
        findInstantHealthPotions();

        if (potionSlots.isEmpty()) {
            return;
        }

        if (originalSlot == -1) {
            originalSlot = mc.player.getInventory().selectedSlot;
        }

        if (multiThrow.getValue()) {
            potsToThrow = calculatePotsNeeded();
            potsThrown = 0;
            isMultiThrowing = true;
        } else {
            potsToThrow = 1;
            potsThrown = 0;
            isMultiThrowing = false;
        }

        if (lookDown.getValue() && !hasRotated) {
            originalPitch = mc.player.getPitch();
            targetPitch = 89.9f;

            if (rotationSpeed.getValueFloat() >= 90) {
                mc.player.setPitch(targetPitch);
                isRotating = false;
                readyToThrow = true;
            } else {
                isRotating = true;
                readyToThrow = true;
            }
            hasRotated = true;
        } else {
            readyToThrow = true;
        }
    }

    private int calculatePotsNeeded() {
        if (mc.player == null) return 1;

        float currentHealth = mc.player.getHealth();

        if (currentHealth <= criticalHealthThreshold.getValueFloat()) {
            return Math.min(criticalHealthPots.getValueInt(), potionSlots.size());
        } else if (currentHealth <= lowHealthThreshold.getValueFloat()) {
            return Math.min(lowHealthPots.getValueInt(), potionSlots.size());
        } else {
            return 1;
        }
    }

    private void executeThrow() {
        throwPotion();
        potsThrown++;

        if (isMultiThrowing && potsThrown < potsToThrow) {
            potTimer.reset();
        } else {
            isThrowing = true;
            timer.reset();
        }

        readyToThrow = false;
    }

    private void throwNextPotion() {
        if (potsThrown < potsToThrow && !potionSlots.isEmpty()) {
            throwPotion();
            potsThrown++;
            potTimer.reset();
        }
    }

    private void throwPotion() {
        int slot;
        if (randomize.getValue()) {
            slot = potionSlots.get(random.nextInt(potionSlots.size()));
        } else {
            if (currentPotionIndex >= potionSlots.size()) {
                currentPotionIndex = 0;
            }
            slot = potionSlots.get(currentPotionIndex);
            currentPotionIndex++;
        }

        mc.player.getInventory().selectedSlot = slot;
    ((MinecraftClientAccessor) mc).invokeDoItemUse();
    }

    private void finishThrow() {
        if (autoSwitch.getValue() && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }

        if (rotateBack.getValue() && hasRotated && !isRotatingBack) {
            if (rotationSpeed.getValueFloat() >= 90) {
                mc.player.setPitch(originalPitch);
                hasRotated = false;
            } else {
                isRotatingBack = true;
            }
        } else if (!rotateBack.getValue()) {
            hasRotated = false;
        }

        isThrowing = false;
    }

    private void finishMultiThrow() {
        if (autoSwitch.getValue() && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }

        if (rotateBack.getValue() && hasRotated && !isRotatingBack) {
            if (rotationSpeed.getValueFloat() >= 90) {
                mc.player.setPitch(originalPitch);
                hasRotated = false;
            } else {
                isRotatingBack = true;
            }
        } else if (!rotateBack.getValue()) {
            hasRotated = false;
        }

        isMultiThrowing = false;
        potsToThrow = 0;
        potsThrown = 0;
    }

    private void findInstantHealthPotions() {
        potionSlots.clear();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isInstantHealthPotion(stack)) {
                potionSlots.add(i);
            }
        }
    }

    private boolean isInstantHealthPotion(ItemStack stack) {
        if (stack.getItem() != Items.SPLASH_POTION) {
            return false;
        }

        PotionContentsComponent potionContents = stack.get(DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) {
            return false;
        }

        if (potionContents.potion().isPresent()) {
            RegistryEntry<Potion> potionEntry = potionContents.potion().get();
            return potionEntry.value().getEffects().stream()
                    .anyMatch(effect -> effect.getEffectType().equals(StatusEffects.INSTANT_HEALTH));
        }

        return potionContents.customEffects().stream()
                .anyMatch(effect -> effect.getEffectType().equals(StatusEffects.INSTANT_HEALTH));
    }

    private boolean isHealthBelowThreshold() {
        if (mc.player == null) {
            return false;
        }
        return mc.player.getHealth() <= healthThreshold.getValueFloat();
    }

    @Override
    public void onEnable() {
        keyPressed = false;
        currentPotionIndex = 0;
        originalSlot = -1;
        originalPitch = 0;
        targetPitch = 0;
        isThrowing = false;
        hasRotated = false;
        isRotating = false;
        isRotatingBack = false;
        readyToThrow = false;
        potsToThrow = 0;
        potsThrown = 0;
        isMultiThrowing = false;
        timer.reset();
        potTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {

        if (autoSwitch.getValue() && originalSlot != -1) {
            mc.player.getInventory().selectedSlot = originalSlot;
        }

        if (hasRotated && rotateBack.getValue()) {
            mc.player.setPitch(originalPitch);
        }

        potionSlots.clear();
        currentPotionIndex = 0;
        originalSlot = -1;
        originalPitch = 0;
        targetPitch = 0;
        keyPressed = false;
        isThrowing = false;
        hasRotated = false;
        isRotating = false;
        isRotatingBack = false;
        readyToThrow = false;
        potsToThrow = 0;
        potsThrown = 0;
        isMultiThrowing = false;
        super.onDisable();
    }

    @Override
    public int getKey() {
        return -1;
    }
} 