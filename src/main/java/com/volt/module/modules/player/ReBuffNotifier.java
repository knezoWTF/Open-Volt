package com.volt.module.modules.player;

import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.math.TimerUtil;
import com.volt.utils.notification.NotificationManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.sound.SoundEvents;

public final class ReBuffNotifier extends Module {

    private final BooleanSetting soundAlert = new BooleanSetting("Sound Alert", true);
    private final BooleanSetting showNotification = new BooleanSetting("Show Notification", true);
    private final NumberSetting volume = new NumberSetting("Volume", 0.1f, 2.0f, 1.0f, 0.1f);

    private final TimerUtil soundTimer = new TimerUtil();
    private boolean isPlayingSound = false;
    private boolean hadSpeed = false;
    private boolean hadStrength = false;

    public ReBuffNotifier() {
        super("ReBuff Notifier", "Plays a sound when speed or strength effects expire", -1, Category.PLAYER);
        this.addSettings(soundAlert, showNotification, volume);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (mc.player == null || !isEnabled()) return;

        boolean hasSpeed = mc.player.hasStatusEffect(StatusEffects.SPEED);
        boolean hasStrength = mc.player.hasStatusEffect(StatusEffects.STRENGTH);

        if (!hasSpeed && hadSpeed) {
            onEffectExpired("Speed");
        }
        if (!hasStrength && hadStrength) {
            onEffectExpired("Strength");
        }

        if (isPlayingSound && soundTimer.hasElapsedTime(3000)) {
            stopSoundNotification();
        }

        hadSpeed = hasSpeed;
        hadStrength = hasStrength;
    }

    private void onEffectExpired(String effectName) {
        if (soundAlert.getValue() && !isPlayingSound) {
            startSoundNotification();
        }

        if (showNotification.getValue()) {
            NotificationManager.getInstance().addBuffExpiredNotification(effectName);
        }
    }

    private void startSoundNotification() {
        if (isPlayingSound) return;

        isPlayingSound = true;
        soundTimer.reset();

        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), volume.getValueFloat()));
    }

    private void stopSoundNotification() {
        isPlayingSound = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        isPlayingSound = false;
        hadSpeed = false;
        hadStrength = false;
        soundTimer.reset();
    }

    @Override
    public void onDisable() {
        stopSoundNotification();
        super.onDisable();
    }
}
