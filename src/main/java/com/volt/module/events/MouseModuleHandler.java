package com.volt.module.events;

import com.volt.Volt;
import com.volt.event.impl.input.MouseClickEvent;
import com.volt.module.Module;
import meteordevelopment.orbit.EventHandler;
import org.lwjgl.glfw.GLFW;

public class MouseModuleHandler {

    @EventHandler
    public void onMouseClick(MouseClickEvent event) {
        if (event.action() == GLFW.GLFW_PRESS) {
            for (Module module : Volt.INSTANCE.getModuleManager().getModules()) {
                if (event.button() == module.getKey()) {
                    module.toggle();
                    break;
                }
            }
        }
    }
}