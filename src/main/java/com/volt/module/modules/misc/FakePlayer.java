package com.volt.module.modules.misc;

import com.mojang.authlib.GameProfile;
import com.volt.event.impl.player.EventAttack;
import com.volt.event.impl.world.WorldChangeEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;

import java.util.UUID;

public class FakePlayer extends Module {
    private final BooleanSetting invincible = new BooleanSetting("Invincible", false);
    private final BooleanSetting criticalHits = new BooleanSetting("Critical Hits", true);
    private final BooleanSetting useTotem = new BooleanSetting("Use Totem", false);
    private OtherClientPlayerEntity fakePlayer;
    private float fakePlayerHealth = 20.0f;
    private long lastHitTime = 0;
    private int hitCount = 0;

    public FakePlayer() {
        super("Fake Player", "Spawns a fake player for making configs (Only works in single player)", Category.MISC);
        addSettings(invincible, criticalHits, useTotem);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        spawnFakePlayer();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        despawnFakePlayer();
    }

    @EventHandler
    private void onWorldChange(WorldChangeEvent event) {
        despawnFakePlayer();
    }


    @EventHandler
    private void onAttack(EventAttack event) {
        if (fakePlayer == null || isNull()) return;

        if (event.getTarget() == fakePlayer) {
            handleFakePlayerHit(isPlayerCriticalHit());
        }
    }

    private boolean isPlayerCriticalHit() {
        if (mc.player == null) return false;

        boolean isFalling = mc.player.getVelocity().y < -0.08F;
        boolean isSneaking = mc.player.isSneaking();
        boolean isOnGround = mc.player.isOnGround();
        boolean isUsingItem = mc.player.isUsingItem();
        boolean isRiding = mc.player.getVehicle() != null;
        boolean isInWater = mc.player.isTouchingWater();
        boolean isInLava = mc.player.isInLava();

        return isFalling && !isSneaking && !isOnGround && !isUsingItem && !isRiding && !isInWater && !isInLava;
    }

    private void handleFakePlayerHit(boolean isCritical) {
        if (System.currentTimeMillis() - lastHitTime < 500) return;
        lastHitTime = System.currentTimeMillis();
        hitCount++;
        boolean shouldPopTotem = false;
        if (useTotem.getValue()) {
            if (invincible.getValue()) {
                if (hitCount % 2 == 0) {
                    shouldPopTotem = true;
                }
            } else {
                float baseDamage = 2.0f + (float) (Math.random() * 4.0f);
                float damage = isCritical ? baseDamage * 1.5f : baseDamage;
                if (fakePlayerHealth - damage <= 0) {
                    shouldPopTotem = true;
                    fakePlayerHealth = 1.0f;
                }
            }
        }
        if (shouldPopTotem) {
            popTotem();
        }
        if (!invincible.getValue() && !shouldPopTotem) {
            float baseDamage = 2.0f + (float) (Math.random() * 4.0f);
            float damage = isCritical ? baseDamage * 1.5f : baseDamage;
            fakePlayerHealth = Math.max(0, fakePlayerHealth - damage);
        }
        addDamageEffects(isCritical);
        if (fakePlayerHealth <= 0 && !invincible.getValue()) {
            respawnFakePlayer();
        }
    }

    private void addDamageEffects(boolean isCritical) {
        if (fakePlayer == null || mc.world == null) return;
        int particleCount = isCritical ? 8 : 5;
        for (int i = 0; i < particleCount; i++) {
            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = Math.random() * 1.8;
            double offsetZ = (Math.random() - 0.5) * 0.5;
            mc.world.addParticle(ParticleTypes.DAMAGE_INDICATOR,
                    fakePlayer.getX() + offsetX,
                    fakePlayer.getY() + offsetY,
                    fakePlayer.getZ() + offsetZ,
                    0, 0, 0);
        }
        if (isCritical && criticalHits.getValue()) {
            for (int i = 0; i < 5; i++) {
                double offsetX = (Math.random() - 0.5) * 0.8;
                double offsetY = Math.random() * 1.8;
                double offsetZ = (Math.random() - 0.5) * 0.8;
                mc.world.addParticle(ParticleTypes.CRIT,
                        fakePlayer.getX() + offsetX,
                        fakePlayer.getY() + offsetY,
                        fakePlayer.getZ() + offsetZ,
                        0, 0, 0);
            }
        }
        if (isCritical && criticalHits.getValue()) {
            mc.world.playSound(mc.player, fakePlayer.getBlockPos(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, fakePlayer.getSoundCategory(),
                    1.0f, 1.0f);
        } else {
            mc.world.playSound(mc.player, fakePlayer.getBlockPos(),
                    SoundEvents.ENTITY_PLAYER_HURT, fakePlayer.getSoundCategory(),
                    1.0f, 1.0f);
        }
        fakePlayer.hurtTime = 10;
    }

    private void respawnFakePlayer() {
        despawnFakePlayer();
        fakePlayerHealth = 20.0f;
        hitCount = 0;
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (this.isEnabled()) {
                    spawnFakePlayer();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void popTotem() {
        if (fakePlayer == null || mc.world == null) return;
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 800, 1));
        mc.world.playSound(fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(),
                SoundEvents.ITEM_TOTEM_USE, fakePlayer.getSoundCategory(),
                1.0f, 1.0f, false);
        for (int i = 0; i < 30; i++) {
            double offsetX = (mc.world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = mc.world.random.nextDouble() * 2.0;
            double offsetZ = (mc.world.random.nextDouble() - 0.5) * 2.0;
            mc.world.addParticle(ParticleTypes.TOTEM_OF_UNDYING,
                    fakePlayer.getX() + offsetX,
                    fakePlayer.getY() + offsetY,
                    fakePlayer.getZ() + offsetZ,
                    0, 0.1, 0);
        }
        fakePlayer.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        new Thread(() -> {
            try {
                Thread.sleep(500);
                if (fakePlayer != null) {
                    fakePlayer.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void spawnFakePlayer() {
        if (isNull()) return;
        if (!mc.isInSingleplayer()) return;
        if (fakePlayer != null) return;
        GameProfile original = mc.player.getGameProfile();
        GameProfile profile = new GameProfile(UUID.randomUUID(), original.getName());
        profile.getProperties().putAll(original.getProperties());

        OtherClientPlayerEntity other = new OtherClientPlayerEntity(mc.world, profile);
        other.copyPositionAndRotation(mc.player);
        other.setYaw(mc.player.getYaw());
        other.setPitch(mc.player.getPitch());

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                other.equipStack(slot, stack.copy());
            }
        }


        mc.world.addEntity(other);

        fakePlayer = other;
    }

    private void despawnFakePlayer() {
        if (fakePlayer == null) return;
        if (!isNull()) {
            fakePlayer.discard();
        }
        fakePlayer = null;
    }
}