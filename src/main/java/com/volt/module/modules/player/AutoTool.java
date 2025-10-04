package com.volt.module.modules.player;


import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class AutoTool extends Module {
    private static final NumberSetting switchDelay = new NumberSetting("Switch Delay", 0, 100, 5, 1);
    private static final BooleanSetting switchForCombat = new BooleanSetting("Switch For Combat", true);
    private static final BooleanSetting returnToPrevious = new BooleanSetting("Return To Previous", true);
    private static final BooleanSetting intelligentSwitching = new BooleanSetting("Intelligent Switching", true);
    private static final BooleanSetting onlyWhenSneaking = new BooleanSetting("Only When Sneaking", false);
    private static final BooleanSetting preventLowDurability = new BooleanSetting("Prevent Low Durability", true);
    private static final NumberSetting durabilityThreshold = new NumberSetting("Durability Threshold", 1, 100, 10, 1);
    private final TimerUtil switchTimer = new TimerUtil();
    private final AtomicReference<Map<ToolType, ToolSlot>> cachedTools = new AtomicReference<>(new EnumMap<>(ToolType.class));
    private ActionState currentState = ActionState.IDLE;
    private int previousSlot = -1;

    public AutoTool() {
        super("Auto Tool", "Automatically switches to the best tool for mining and combat", -1, Category.PLAYER);
        this.addSettings(switchDelay, switchForCombat, returnToPrevious,
                intelligentSwitching, onlyWhenSneaking, preventLowDurability, durabilityThreshold);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (onlyWhenSneaking.getValue() && !mc.player.isSneaking()) {
            return;
        }

        if (!mc.options.attackKey.isPressed()) {
            if (currentState != ActionState.IDLE && returnToPrevious.getValue() && previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            currentState = ActionState.IDLE;
            return;
        }

        HitResult hitResult = mc.crosshairTarget;
        if (hitResult == null) return;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            handleMining((BlockHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.ENTITY && switchForCombat.getValue()) {
            handleCombat((EntityHitResult) hitResult);
        }
    }

    private void handleMining(BlockHitResult blockHit) {
        BlockPos blockPos = blockHit.getBlockPos();
        BlockState blockState = mc.world.getBlockState(blockPos);

        if (blockState.getHardness(mc.world, blockPos) < 0) return;

        int bestToolSlot = findBestTool(blockState);
        if (bestToolSlot != -1 && bestToolSlot != mc.player.getInventory().selectedSlot) {
            if (!switchTimer.hasElapsedTime(switchDelay.getValueInt())) return;

            if (intelligentSwitching.getValue()) {
                ItemStack currentTool = mc.player.getMainHandStack();
                if (!currentTool.isEmpty()) {
                    ItemStack newTool = mc.player.getInventory().getStack(bestToolSlot);
                    float currentScore = calculateToolScore(currentTool, blockState);
                    float newScore = calculateToolScore(newTool, blockState);
                    if (newScore <= currentScore * 1.15f) {
                        return;
                    }
                }
            }

            if (currentState == ActionState.IDLE) {
                previousSlot = mc.player.getInventory().selectedSlot;
            }
            mc.player.getInventory().selectedSlot = bestToolSlot;
            currentState = ActionState.MINING;
            switchTimer.reset();
        }
    }

    private void handleCombat(EntityHitResult entityHit) {
        Entity entity = entityHit.getEntity();
        if (!(entity instanceof LivingEntity) || entity == mc.player) return;

        int bestWeaponSlot = findBestWeapon();
        if (bestWeaponSlot != -1 && bestWeaponSlot != mc.player.getInventory().selectedSlot) {
            if (!switchTimer.hasElapsedTime(switchDelay.getValueInt())) return;

            mc.player.getInventory().selectedSlot = bestWeaponSlot;
            currentState = ActionState.ATTACKING;
            switchTimer.reset();
        }
    }

    private int findBestTool(BlockState blockState) {
        int bestSlot = -1;
        float bestScore = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            if (preventLowDurability.getValue() && hasLowDurability(stack)) continue;

            if (isToolEffective(stack, blockState)) {
                float score = calculateToolScore(stack, blockState);
                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    private int findBestWeapon() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SwordItem) {
                if (!preventLowDurability.getValue() || !hasLowDurability(stack)) {
                    return i;
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AxeItem) {
                if (!preventLowDurability.getValue() || !hasLowDurability(stack)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private boolean isToolEffective(ItemStack tool, BlockState blockState) {
        return tool.isSuitableFor(blockState) || tool.getMiningSpeedMultiplier(blockState) > 1.0f;
    }

    private float calculateToolScore(ItemStack tool, BlockState blockState) {
        float baseScore = blockState != null ? tool.getMiningSpeedMultiplier(blockState) : 1.0f;

        float durabilityMultiplier = 1.0f;
        if (tool.getMaxDamage() > 0) {
            float durabilityRatio = (float) (tool.getMaxDamage() - tool.getDamage()) / tool.getMaxDamage();
            durabilityMultiplier = 0.8f + (durabilityRatio * 0.2f);
        }

        float materialBonus = getToolMaterialBonus(tool);

        return baseScore * durabilityMultiplier * materialBonus;
    }

    private float getToolMaterialBonus(ItemStack tool) {
        Item item = tool.getItem();

        if (item.toString().contains("netherite")) return 6.0f;
        if (item.toString().contains("diamond")) return 5.0f;
        if (item.toString().contains("iron")) return 4.0f;
        if (item.toString().contains("golden")) return 3.5f;
        if (item.toString().contains("stone")) return 2.0f;
        if (item.toString().contains("wooden")) return 1.5f;

        return 1.0f;
    }

    private boolean hasLowDurability(ItemStack stack) {
        if (stack.getMaxDamage() <= 0) return false;

        int remaining = stack.getMaxDamage() - stack.getDamage();
        int threshold = durabilityThreshold.getValueInt();

        return remaining <= threshold;
    }

    @Override
    public void onEnable() {
        switchTimer.reset();
        cachedTools.get().clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        currentState = ActionState.IDLE;
        previousSlot = -1;
        cachedTools.get().clear();
        super.onDisable();
    }

    private enum ActionState {
        IDLE,
        MINING,
        ATTACKING
    }

    private enum ToolType {

    }

    private record ToolSlot(ItemStack stack, int slot, float score) {
    }
} 