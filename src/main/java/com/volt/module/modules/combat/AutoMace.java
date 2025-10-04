package com.volt.module.modules.combat;


import com.volt.event.impl.player.TickEvent;
import com.volt.mixin.MinecraftClientAccessor;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.modules.misc.Teams;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.MathUtils;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class AutoMace extends Module {

    private final NumberSetting minFallDistance = new NumberSetting("Min Fall Distance", 1.0, 10.0, 3.0, 0.5);
    private final NumberSetting switchDelayMin = new NumberSetting("Switch Delay Min", 50, 500, 100, 10);
    private final NumberSetting switchDelayMax = new NumberSetting("Switch Delay Max", 100, 1000, 200, 10);
    private final NumberSetting attackDelayMin = new NumberSetting("Attack Delay Min", 50, 300, 80, 10);
    private final NumberSetting attackDelayMax = new NumberSetting("Attack Delay Max", 100, 500, 150, 10);
    private final BooleanSetting targetPlayers = new BooleanSetting("Target Players", true);
    private final BooleanSetting targetMobs = new BooleanSetting("Target Mobs", false);
    private final BooleanSetting autoSwitchBack = new BooleanSetting("Auto Switch Back", true);
    private final BooleanSetting instantSwitchBack = new BooleanSetting("Instant Switch Back", true);
    private final BooleanSetting ignorePassiveMobs = new BooleanSetting("No Passive Mobs", true);
    private final BooleanSetting ignoreInvisible = new BooleanSetting("No Invisible", true);
    private final BooleanSetting respectCooldown = new BooleanSetting("Respect Cooldown", true);
    private final TimerUtil switchTimer = new TimerUtil();
    private final TimerUtil attackTimer = new TimerUtil();
    private int previousSlot = -1;
    private boolean hadMaceEquipped = false;
    private Entity currentTarget = null;
    private double fallStartY = -1;
    private boolean isInAir = false;
    private boolean hasSwitchedToMace = false;

    public AutoMace() {
        super("Auto Mace", "Automatically attacks with mace", -1, Category.COMBAT);
        this.addSettings(minFallDistance, switchDelayMin, switchDelayMax, attackDelayMin, attackDelayMax,
                targetPlayers, targetMobs, autoSwitchBack, instantSwitchBack, ignorePassiveMobs, ignoreInvisible, respectCooldown);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;

        if (switchDelayMin.getValueInt() >= switchDelayMax.getValueInt()) {
            switchDelayMax.setValue(switchDelayMin.getValueInt() + 1);
        }
        if (attackDelayMin.getValue() >= attackDelayMax.getValue()) {
            attackDelayMax.setValue(attackDelayMin.getValue() + 1);
        }
        updateFallTracking();

        currentTarget = mc.targetedEntity;

        boolean isAirborne = !mc.player.isOnGround();
        boolean isFalling = mc.player.getVelocity().y < -0.1;
        double currentFallDistance = getCurrentFallDistance();

        if (isAirborne && isFalling && currentFallDistance >= minFallDistance.getValueFloat()) {
            if (hasValidTarget(currentTarget)) {
                if (!isMaceEquipped() && !hasSwitchedToMace) {
                    if (canSwitchWeapon()) {
                        storePreviousSlot();
                        switchToMace();
                        hasSwitchedToMace = true;
                    }
                }

                if (isMaceEquipped() && shouldAttack()) {
                    attackTarget();
                }
            } else {
                handleNoTarget();
            }
        } else if (mc.player.isOnGround()) {
            handleLanding();
        } else if (isAirborne && !isFalling) {
            handleNotFalling();
        }
    }

    private void updateFallTracking() {
        boolean currentlyOnGround = mc.player.isOnGround();
        boolean currentlyFalling = mc.player.getVelocity().y < -0.1;
        double currentY = mc.player.getY();

        if (currentlyOnGround) {
            if (isInAir) {
                isInAir = false;
                fallStartY = -1;
            }
        } else {
            if (!isInAir) {
                isInAir = true;
                fallStartY = currentY;
            } else {
                if (currentlyFalling) {
                    if (fallStartY == -1 || currentY > fallStartY) {
                        fallStartY = currentY;
                    }
                } else if (mc.player.getVelocity().y > 0.1) {
                    fallStartY = Math.max(fallStartY == -1 ? currentY : fallStartY, currentY);
                }
            }
        }
    }

    private double getCurrentFallDistance() {
        if (!isInAir || fallStartY == -1) {
            return 0;
        }
        double currentY = mc.player.getY();
        return Math.max(0, fallStartY - currentY);
    }

    private boolean hasValidTarget(Entity entity) {
        if (entity == null || entity == mc.player || entity == mc.cameraEntity) return false;
        if (!(entity instanceof LivingEntity livingEntity)) return false;
        if (!livingEntity.isAlive() || livingEntity.isDead()) return false;
        if (Teams.isTeammate(entity)) return false;

        if (entity instanceof PlayerEntity) {
            if (!targetPlayers.getValue()) return false;
        } else {
            if (!targetMobs.getValue()) return false;
            if (ignorePassiveMobs.getValue() && entity instanceof PassiveEntity) return false;
            if (entity instanceof Tameable) return false;
        }

        return !ignoreInvisible.getValue() || !entity.isInvisible();
    }

    private boolean canSwitchWeapon() {
        long switchDelay = (long) MathUtils.randomDoubleBetween(switchDelayMin.getValueInt(), switchDelayMax.getValueInt() + 1);
        return switchTimer.hasElapsedTime(switchDelay, true);
    }

    private boolean shouldAttack() {
        if (currentTarget == null) return false;

        if (respectCooldown.getValue()) {
            float cooldown = mc.player.getAttackCooldownProgress(0.0f);
            if (cooldown < 0.9f) return false;
        }
        double attackDelay = MathUtils.randomDoubleBetween(attackDelayMin.getValue(), attackDelayMax.getValue() + 1);
        return attackTimer.hasElapsedTime((long) attackDelay, true);
    }

    private void attackTarget() {
        if (currentTarget == null) return;
        ((MinecraftClientAccessor) mc).invokeDoAttack();
    }

    private void handleLanding() {
        isInAir = false;
        fallStartY = -1;
        hasSwitchedToMace = false;

        if (autoSwitchBack.getValue() && previousSlot != -1 && !hadMaceEquipped) {
            switchToSlot(previousSlot);
            previousSlot = -1;
        }

        currentTarget = null;
    }

    private void handleNoTarget() {
        if (autoSwitchBack.getValue() && instantSwitchBack.getValue() && previousSlot != -1 && !hadMaceEquipped && isMaceEquipped()) {
            switchToSlot(previousSlot);
            previousSlot = -1;
            hasSwitchedToMace = false;
        }
    }

    private void handleNotFalling() {
        if (autoSwitchBack.getValue() && instantSwitchBack.getValue() && previousSlot != -1 && !hadMaceEquipped && isMaceEquipped()) {
            switchToSlot(previousSlot);
            previousSlot = -1;
            hasSwitchedToMace = false;
        }
    }

    private boolean isMaceEquipped() {
        ItemStack mainHand = mc.player.getMainHandStack();
        return mainHand.getItem() == Items.MACE;
    }

    private void storePreviousSlot() {
        if (!isMaceEquipped() && previousSlot == -1) {
            previousSlot = mc.player.getInventory().selectedSlot;
            hadMaceEquipped = false;
        } else if (isMaceEquipped()) {
            hadMaceEquipped = true;
        }
    }

    private void switchToMace() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.MACE) {
                mc.player.getInventory().selectedSlot = i;
                return;
            }
        }
    }

    private void switchToSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }

    @Override
    public void onEnable() {
        previousSlot = -1;
        hadMaceEquipped = false;
        currentTarget = null;
        fallStartY = -1;
        isInAir = false;
        hasSwitchedToMace = false;
        switchTimer.reset();
        attackTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (autoSwitchBack.getValue() && previousSlot != -1 && !hadMaceEquipped) {
            switchToSlot(previousSlot);
        }

        previousSlot = -1;
        hadMaceEquipped = false;
        currentTarget = null;
        fallStartY = -1;
        isInAir = false;
        hasSwitchedToMace = false;
        switchTimer.reset();
        attackTimer.reset();
        super.onDisable();
    }
}
