package com.volt.module.modules.movement;

import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public final class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("No Jump Delay", "Removes the delay between jumps", -1, Category.MOVEMENT);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;
        if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            mc.options.jumpKey.setPressed(false);
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }
        }
    }
}
