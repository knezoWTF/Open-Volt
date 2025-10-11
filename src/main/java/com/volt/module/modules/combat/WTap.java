package com.volt.module.modules.combat;


import com.volt.event.impl.player.AttackEvent;
import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.keybinding.KeyUtils;
import com.volt.utils.math.TimerUtil;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public class WTap extends Module {
    public static final NumberSetting chance = new NumberSetting("Chance (%)", 1, 100, 100, 1);
    private final NumberSetting msDelay = new NumberSetting("Ms", 1, 500, 60, 1);
    private final BooleanSetting onlyOnGround = new BooleanSetting("Only on ground", true);
    boolean wasSprinting;
    TimerUtil timer = new TimerUtil();

    public WTap() {
        super("WTap", "Makes you automatically WTAP", -1, Category.COMBAT);
        this.addSettings(msDelay, chance, onlyOnGround);
    }

    @EventHandler
    private void onAttackEvent(AttackEvent event) {
        if (isNull()) return;
        if (Math.random() * 100 > chance.getValueFloat()) return;
        var target = mc.targetedEntity;
        if (!mc.player.isOnGround() && onlyOnGround.getValue()) return;
        if (target == null) return;
        if (!target.isAlive()) return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W)) return;
        if (mc.player.isSprinting()) {
            wasSprinting = true;
            mc.options.forwardKey.setPressed(false);
        }
    }


    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (!KeyUtils.isKeyPressed(GLFW.GLFW_KEY_W)) return;

        if (wasSprinting) {
            if (timer.hasElapsedTime(msDelay.getValueInt(), true)) {
                mc.options.forwardKey.setPressed(true);
                wasSprinting = false;
            }
        }
    }
}
