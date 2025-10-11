package com.volt.module.modules.combat;


import com.volt.event.impl.network.EventPacket;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

public final class Velocity extends Module {
    public static final NumberSetting chance = new NumberSetting("Chance (%)", 1, 100, 100, 1);
    public static final BooleanSetting ignoreWhenBackwards = new BooleanSetting("Ignore S press", true);
    public static final BooleanSetting ignoreOnFire = new BooleanSetting("Ignore on fire", true);
    private boolean jumped = false;

    public Velocity() {
        super("Velocity", "Automatically jump resets to reduce your velocity", -1, Category.COMBAT);
        this.addSettings(chance, ignoreWhenBackwards, ignoreOnFire);
    }

    @EventHandler
    private void onPacketEvent(EventPacket event) {
        if (isNull()) return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet && packet.getEntityId() == mc.player.getId()) {
            if (chanceCheck()) {
                if (ignoreWhenBackwards.getValue() && mc.options.backKey.isPressed()) return;
                if (ignoreOnFire.getValue() && mc.player.isOnFire()) return;
                if (mc.currentScreen != null) return;

                if (!jumped && mc.player.isOnGround()) {
                    KeyBinding.setKeyPressed(mc.options.jumpKey.getDefaultKey(), true);
                    jumped = true;
                }
                if (jumped) {
                    KeyBinding.setKeyPressed(mc.options.jumpKey.getDefaultKey(), false);
                    jumped = false;
                }
            }
        }
    }

    private boolean chanceCheck() {
        return (Math.random() * 100 <= chance.getValueFloat());
    }
}
