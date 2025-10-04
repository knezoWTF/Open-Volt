package com.volt.module.modules.combat;


import com.volt.event.impl.player.EventAttack;
import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public class STap extends Module {
    public static final NumberSetting chance = new NumberSetting("Chance (%)", 1, 100, 100, 1);
    private final NumberSetting msDelay = new NumberSetting("Ms", 1, 500, 60, 1);
    private final BooleanSetting onlyOnGround = new BooleanSetting("Only on ground", true);
    boolean wasSprinting;
    TimerUtil timer = new TimerUtil();

    public STap() {
        super("STap", "Makes you automatically STAP", -1, Category.COMBAT);
        this.addSettings(msDelay, chance, onlyOnGround);
    }

    @EventHandler
    private void onAttackEvent(EventAttack event) {
        if (isNull()) return;
        var target = mc.targetedEntity;
        if (target == null) return;
        if (!target.isAlive()) return;
        if (!mc.player.isOnGround() && onlyOnGround.getValue()) return;
        if (Math.random() * 100 > chance.getValueFloat()) return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W)) return;
        if (mc.player.isSprinting()) {
            wasSprinting = true;
            mc.options.backKey.setPressed(true);
        }
    }


    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W)) return;
        if (timer.hasElapsedTime(msDelay.getValueInt(), true)) {
            if (wasSprinting) {
                mc.options.backKey.setPressed(false);
                wasSprinting = false;
            }
        }
    }
}