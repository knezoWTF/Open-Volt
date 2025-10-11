package com.volt.module.modules.combat;

import com.volt.event.impl.player.AttackEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.hit.HitResult;

public final class AntiMiss extends Module {
    public AntiMiss() {
        super("Anti Miss", "Makes you not miss", -1, Category.COMBAT);
    }

    @EventHandler
    private void onAttackEvent(AttackEvent event) {
        if (isNull()) return;

        assert mc.crosshairTarget != null;
        if (mc.crosshairTarget.getType().equals(HitResult.Type.MISS)) {
            event.cancel();
        }
    }
}
