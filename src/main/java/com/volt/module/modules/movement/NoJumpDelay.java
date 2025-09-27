package com.volt.module.modules.movement;

import com.volt.Volt;
import com.volt.event.impl.player.TickEvent;
import com.volt.module.Category;
import com.volt.module.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

public final class NoJumpDelay extends Module {
    private final ModeWatcher modeWatcher = new ModeWatcher();
    private boolean autoDisabled;

    public NoJumpDelay() {
        super("No Jump Delay", "Removes the delay between jumps", -1, Category.MOVEMENT);
        Volt.INSTANCE.getVoltEventBus().subscribe(modeWatcher);
    }

    @EventHandler
    private void onTickEvent(TickEvent event) {
        if (isNull()) return;
        if (mc.currentScreen != null) return;
        if (!isSurvivalLike()) return;
        if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            mc.options.jumpKey.setPressed(false);
            if (mc.player.isOnGround()) {
                mc.player.jump();
            }
        }
    }

    private boolean isSurvivalLike() {
        if (isNull()) return false;
        if (mc.interactionManager == null) return false;
        GameMode mode = mc.interactionManager.getCurrentGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    private final class ModeWatcher {
        @EventHandler
        private void onTickEvent(TickEvent event) {
            if (isNull()) return;
            if (!isSurvivalLike()) {
                if (isEnabled() && !autoDisabled) {
                    autoDisabled = true;
                    mc.execute(() -> {
                        if (isEnabled()) {
                            setEnabled(false);
                        }
                    });
                }
            } else if (autoDisabled && !isEnabled()) {
                mc.execute(() -> {
                    if (!isEnabled()) {
                        setEnabled(true);
                    }
                    autoDisabled = false;
                });
            }
        }
    }
}
