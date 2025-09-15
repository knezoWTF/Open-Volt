package com.volt.module.modules.movement;

import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import meteordevelopment.orbit.EventHandler;

public final class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Makes you automatically sprint", -1, Category.MOVEMENT);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.options.getSprintToggled().getValue()) mc.options.getSprintToggled().setValue(false);
        
        mc.options.sprintKey.setPressed(true);
    }
}
