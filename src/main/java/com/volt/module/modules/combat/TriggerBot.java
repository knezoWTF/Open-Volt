package com.volt.module.modules.combat;

import com.volt.Volt;
import com.volt.event.impl.player.TickEvent;
import com.volt.event.impl.world.WorldChangeEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.friend.FriendManager;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.mc.CombatUtil;
import com.volt.module.modules.misc.Teams;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;

public final class TriggerBot extends Module {
    public static final NumberSetting swordThreshold = new NumberSetting("Sword Threshold", 0.1, 1, 0.95, 0.01);
    public static final NumberSetting axeThreshold = new NumberSetting("Axe Threshold", 0.1, 1, 0.95, 0.01);
    public static final NumberSetting reactionTime = new NumberSetting("Reaction Time", 1, 350, 30, 0.5);
    public static final BooleanSetting intelligentCooldown = new BooleanSetting("Intelligent Cooldown", false);
    public static final BooleanSetting preferCrits = new BooleanSetting("Prefer Crits", false);
    public static final NumberSetting preferCritsMin = new NumberSetting("CritMin Cooldown", 0.1, 1, 0.90, 0.01);
    public static final BooleanSetting forcePreferCrits = new BooleanSetting("Force Prefer Crits", false);
    public static final BooleanSetting ignorePassiveMobs = new BooleanSetting("No Passive", false);
    public static final BooleanSetting ignoreInvisible = new BooleanSetting("No Invisible", false);
    public static final BooleanSetting ignoreCrystals = new BooleanSetting("No Crystals", false);
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
    private float randomizedThreshold = 0;
    private Entity target;
    private String lastTargetUUID = null;
    private boolean wasSprinting = false;

    public TriggerBot() {
        super("Trigger Bot", "Automatically attacks once aimed at a target", -1, Category.COMBAT);
        addSettings(
                swordThreshold, axeThreshold, reactionTime, intelligentCooldown, ignorePassiveMobs,
                ignoreCrystals, respectShields, preferCrits, preferCritsMin, forcePreferCrits,
                ignoreInvisible, onlyWhenMouseDown, useOnlySwordOrAxe, disableOnWorldChange, samePlayer
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
        if (isNull() || mc.player.isUsingItem() || mc.currentScreen != null) return;
        BreachSwap breachSwap = (BreachSwap) Volt.INSTANCE.getModuleManager().getModule(BreachSwap.class).get();
        if (breachSwap != null && breachSwap.isEnabled() && breachSwap.isSwapping()) return;

        target = mc.targetedEntity;
        if (target == null || !isHoldingSwordOrAxe()) return;
        if (onlyWhenMouseDown.getValue() && !mc.options.attackKey.isPressed()) return;
        if (!hasTarget(target)) return;

        if (respectShields.getValue()) {
            Item item = mc.player.getMainHandStack().getItem();
            if (target instanceof PlayerEntity playerTarget &&
                CombatUtil.isShieldFacingAway(playerTarget) &&
                item instanceof SwordItem) {
                return;
            }
        }

        if (setPreferCrits()) {
            performAttack();
            return;
        }

        if (!waitingForReaction) {
            waitingForReaction = true;
            timerReactionTime.reset();
            if (intelligentCooldown.getValue() && waitingForDelay) {
                long elapsed = timer.getElapsedTime();
                currentReactionDelay = Math.max(0, currentReactionDelay - elapsed);
            } else {
                currentReactionDelay = reactionTime.getValueInt();
            }
        }

        if (waitingForReaction && timerReactionTime.hasElapsedTime(currentReactionDelay, true)) {
            if (hasElapsedDelay() && samePlayerCheck(target)) {
                performAttack();
                waitingForReaction = false;
            }
        }
    }

    public boolean hasTarget(Entity en) {
        if (en == mc.player || en == mc.cameraEntity || !en.isAlive()) return false;
        if (en instanceof WindChargeEntity) return false;
        if (en instanceof PlayerEntity player && FriendManager.isFriend(player.getUuid())) return false;
        if (Teams.isTeammate(en)) return false;
        return switch (en) {
            case EndCrystalEntity ignored when ignoreCrystals.getValue() -> false;
            case Tameable ignored -> false;
            case PassiveEntity ignored when ignorePassiveMobs.getValue() -> false;
            default -> !ignoreInvisible.getValue() || !en.isInvisible();
        };
    }

    private boolean setPreferCrits() {
        if (!preferCrits.getValue() || mc.player == null) return false;
        if (mc.player.hasStatusEffect(StatusEffects.LEVITATION)
                || mc.player.isSneaking()
                || mc.player.isUsingItem()
                || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || mc.player.getVehicle() != null
                || mc.player.isTouchingWater()
                || mc.player.isInLava()) {
            return false;
        }

        boolean isCooldownCharged = mc.player.getAttackCooldownProgress(0.0F) >= preferCritsMin.getValueFloat();
        boolean isFalling = mc.player.getVelocity().y < -0.08F;
        boolean inCobweb = mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.COBWEB
                || mc.world.getBlockState(mc.player.getBlockPos().up()).getBlock() == Blocks.COBWEB
                || mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() == Blocks.COBWEB;
        boolean isFallFlying = mc.player.isFallFlying();

        return isCooldownCharged && (isFalling || inCobweb || isFallFlying) && !mc.player.isOnGround();
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
                randomizedThreshold = axeThreshold.getValueFloat();
                waitingForDelay = true;
            }
            if (cooldown >= randomizedThreshold) {
                waitingForDelay = false;
                return true;
            } else {
                timer.reset();
            }
            return false;
        } else {
            swordDelay = swordThreshold.getValueFloat();
            return cooldown >= swordDelay;
        }
    }

    private boolean isHoldingSwordOrAxe() {
        if (!useOnlySwordOrAxe.getValue()) return true;
        Item item = mc.player.getMainHandStack().getItem();
        return item instanceof AxeItem || item instanceof SwordItem;
    }

    private void performAttack() {
        if (forcePreferCrits.getValue() && setPreferCrits()) {
            wasSprinting = mc.player.isSprinting();
            if (wasSprinting) mc.options.sprintKey.setPressed(false);

            ((MinecraftClientAccessor) mc).invokeDoAttack();

            if (wasSprinting) mc.options.sprintKey.setPressed(true);
        } else {
            ((MinecraftClientAccessor) mc).invokeDoAttack();
        }

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
