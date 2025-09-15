package com.volt.module.modules.movement;

import org.lwjgl.glfw.GLFW;

import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.utils.mc.MouseSimulation;

import meteordevelopment.orbit.EventHandler;

public final class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Makes you automatically sprint", -1, Category.MOVEMENT);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull())
            return;
        mc.options.sprintKey.setPressed(true);
    }
}
