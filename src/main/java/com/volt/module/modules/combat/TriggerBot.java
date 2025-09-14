package com.volt.module.modules.combat;


import org.lwjgl.glfw.GLFW;

import com.volt.event.impl.player.TickEvent;
import com.volt.event.impl.world.WorldChangeEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.friend.FriendManager;
import com.volt.utils.math.MathUtils;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.CombatUtil;
import com.volt.utils.mc.MouseSimulation;
import com.volt.module.modules.misc.Teams;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;

/**
 * @author graph
 */

public final class TriggerBot extends Module {
    public static final NumberSetting swordThresholdMax = new NumberSetting("Sword Threshold Max", 0.1, 1, 0.95, 0.01);
    public static final NumberSetting swordThresholdMin = new NumberSetting("Sword Threshold Min", 0.1, 1, 0.90, 0.01);
    public static final NumberSetting axeThresholdMax = new NumberSetting("Axe Threshold Max", 0.1, 1, 0.95, 0.01);
    public static final NumberSetting axeThresholdMin = new NumberSetting("Axe Threshold Min", 0.1, 1, 0.90, 0.01);
    public static final NumberSetting axePostDelayMax = new NumberSetting("Axe Post Max", 1, 500, 120, 0.5);
    public static final NumberSetting axePostDelayMin = new NumberSetting("Axe Post Min", 1, 500, 120, 0.5);
    public static final NumberSetting reactionTimeMax = new NumberSetting("Reaction Time Max", 1, 350, 95, 0.5);
    public static final NumberSetting reactionTimeMin = new NumberSetting("Reaction Time Min", 1, 350, 20, 0.5);
    public static final BooleanSetting preferCrits = new BooleanSetting("Prefer Crits", false);
    public static final NumberSetting preferCritsMin = new NumberSetting("CritMin Cooldown", 0.1, 1, 0.90, 0.01);
    public static final BooleanSetting ignorePassiveMobs = new BooleanSetting("No Passive", true);
    public static final BooleanSetting ignoreInvisible = new BooleanSetting("No Invisible", true);
    public static final BooleanSetting ignoreCrystals = new BooleanSetting("No Crystals", true);
    public static final BooleanSetting respectShields = new BooleanSetting("Ignore Shields", false);
    public static final BooleanSetting useOnlySwordOrAxe = new BooleanSetting("Only Sword/Axe", true);
    public static final BooleanSetting onlyWhenMouseDown = new BooleanSetting("Only Mouse Hold", false);
    public static final BooleanSetting disableOnWorldChange = new BooleanSetting("Disable on Load", false);
    public static final BooleanSetting samePlayer = new BooleanSetting("Same Player", false);
    private final TimerUtil timer = new TimerUtil();
    private final TimerUtil samePlayerTimer = new TimerUtil();
    private final TimerUtil timerReactionTime = new TimerUtil();
    public boolean waitingForDelay = false;
    private boolean waitingForReaction = false;
    private long currentReactionDelay = 0;
    private float swordDelay = 0;
    private float randomizedPostDelay = 0;
    private float randomizedThreshold = 0;
    private Entity target;
    private String lastTargetUUID = null;

    public TriggerBot() {
        super("Trigger Bot", "Makes you automatically attack once aimed at a target", -1, Category.COMBAT);
        addSettings(
                swordThresholdMax, swordThresholdMin, axeThresholdMax, axeThresholdMin, axePostDelayMax, axePostDelayMin,reactionTimeMax,reactionTimeMin,ignorePassiveMobs,
                ignoreCrystals, respectShields, preferCrits,preferCritsMin,  ignoreInvisible, onlyWhenMouseDown, useOnlySwordOrAxe, disableOnWorldChange, samePlayer
        );
    }

    @EventHandler
    private void onWorldChangeEvent(WorldChangeEvent event) {
        if (disableOnWorldChange.getValue() && this.isEnabled()) {
            this.toggle();
        }
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull() || mc.player.isUsingItem()) return;
        if (mc.currentScreen != null) return;
        if (axeThresholdMin.getValueFloat() >= axeThresholdMax.getValueFloat()) {
            axeThresholdMin.setValue(axeThresholdMax.getValueFloat() - 0.05f);
        }
        if (swordThresholdMin.getValueFloat() >= swordThresholdMax.getValueFloat()) {
            swordThresholdMin.setValue(swordThresholdMax.getValueFloat() - 0.05f);
        }
        if (axePostDelayMin.getValueFloat() >= axePostDelayMax.getValueFloat()) {
            axePostDelayMin.setValue(axePostDelayMax.getValueFloat() - 0.5f);
        }
        if (reactionTimeMin.getValueFloat() >= reactionTimeMax.getValueFloat()) {
            reactionTimeMin.setValue(reactionTimeMax.getValueFloat() - 0.5f);
        }
        target = mc.targetedEntity;
        if (target == null) return;
        if (!isHoldingSwordOrAxe()) return;
        if (onlyWhenMouseDown.getValue() && !mc.options.attackKey.isPressed()) return;
        if (!hasTarget(target)) return;
        if (respectShields.getValue()) {
            Item item = mc.player.getMainHandStack().getItem();
            if (target instanceof PlayerEntity playerTarget && CombatUtil.isShieldFacingAway(((LivingEntity)playerTarget)) && item instanceof SwordItem) {
                return;
            }
        }
        if (setPreferCrits()) {
            ((MinecraftClientAccessor) mc).invokeDoAttack();
            return;
        }

        if (!waitingForReaction) {
            waitingForReaction = true;
            timerReactionTime.reset();
            currentReactionDelay = (long) MathUtils.randomDoubleBetween(reactionTimeMin.getValue(), reactionTimeMax.getValue());
        }
        if (waitingForReaction && timerReactionTime.hasElapsedTime(currentReactionDelay, true)) {
            if (hasElapsedDelay()) {
                if (hasTarget(target) && samePlayerCheck(target)) {
                    attack();
                    waitingForReaction = false;
                }
            }
        }
}


    public boolean hasTarget(Entity en) {
        if (en == mc.player || en == mc.cameraEntity || !en.isAlive()) return false;
        if (en instanceof PlayerEntity player && FriendManager.isFriend(player.getUuid())) return false;
        if (Teams.isTeammate(en)) return false;
        return switch (en) {
            case EndCrystalEntity endCrystalEntity when ignoreCrystals.getValue() -> false;
            case Tameable tameable -> false;
            case PassiveEntity passiveEntity when ignorePassiveMobs.getValue() -> false;
            default -> !ignoreInvisible.getValue() || !en.isInvisible();
        };
    }

private boolean setPreferCrits() {
    if (!preferCrits.getValue()) return false;

    assert mc.player != null;

    boolean isSneaking = mc.player.isSneaking();
    boolean isUsingItem = mc.player.isUsingItem();
    boolean hasBlindness = mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
    boolean isRiding = mc.player.getVehicle() != null;
    boolean isInWater = mc.player.isTouchingWater();
    boolean isInLava = mc.player.isInLava();
    boolean isCooldownCharged = mc.player.getAttackCooldownProgress(0.0F) >= preferCritsMin.getValueFloat();

    boolean isFalling = mc.player.getVelocity().y < -0.08F;
    boolean inCobweb = mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB
            || mc.world.getBlockState(mc.player.getBlockPos().up()).getBlock() == Blocks.COBWEB
            || mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() == Blocks.COBWEB;
    boolean isFallFlying = mc.player.isFallFlying();

    return (isFalling || inCobweb || isFallFlying)
            && !isSneaking
            && !mc.player.isOnGround()
            && !isUsingItem
            && !hasBlindness
            && !isRiding
            && !isInWater
            && !isInLava
            && isCooldownCharged;
}



    private boolean samePlayerCheck(Entity entity) {
        if (!samePlayer.getValue()) return true;
        if (entity == null) return false;

        if (samePlayerTimer.hasElapsedTime(3000, false)) {
            lastTargetUUID = null;
            return true;
        }

        return entity.getUuidAsString().equals(lastTargetUUID);
    }

    private boolean hasElapsedDelay() {
        if (setPreferCrits()) return false;

        Item heldItem = mc.player.getMainHandStack().getItem();
        float cooldown = mc.player.getAttackCooldownProgress(0.0f);

        if (heldItem instanceof AxeItem) {
            if (!waitingForDelay) {
                randomizedThreshold = (float) MathUtils.randomDoubleBetween(axeThresholdMin.getValueFloat(), axeThresholdMax.getValueFloat());
                randomizedPostDelay = (float) MathUtils.randomDoubleBetween(axePostDelayMin.getValueFloat(), axePostDelayMax.getValueFloat());
                waitingForDelay = true;
            }
            if (cooldown >= randomizedThreshold) {
                if (timer.hasElapsedTime((long) randomizedPostDelay, true)) {
                    waitingForDelay = false;
                    return true;
                }
            } else {
                timer.reset();
            }
            return false;
        } else {
            swordDelay = (float) MathUtils.randomDoubleBetween(swordThresholdMin.getValueFloat(), swordThresholdMax.getValueFloat());
            return cooldown >= swordDelay;
        }
    }

    private boolean isHoldingSwordOrAxe() {
        if (!useOnlySwordOrAxe.getValue()) return true;
        assert mc.player != null;
        Item item = mc.player.getMainHandStack().getItem();
        return item instanceof AxeItem || item instanceof SwordItem;
    }
        public void attack() {
    ((MinecraftClientAccessor) mc).invokeDoAttack();
    MouseSimulation.mouseClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        if (samePlayer.getValue() && target != null) {
            lastTargetUUID = target.getUuidAsString();
            samePlayerTimer.reset();
        }
        waitingForDelay = false;
}
    @Override
    public void onEnable() {
        timer.reset();
        timerReactionTime.reset();
        waitingForReaction = false;
        waitingForDelay = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        timer.reset();
        timerReactionTime.reset();
        waitingForReaction = false;
        waitingForDelay = false;
        super.onDisable();
    }
}
