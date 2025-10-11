package com.volt.module.modules.client;

import com.volt.gui.ClickGui;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.ModeSetting;
import org.lwjgl.glfw.GLFW;

public final class ClickGUIModule extends Module {
    private static final ModeSetting scale = new ModeSetting("Scale", "Small", "Small", "Medium", "Large");
    int prevGuiScale;

    public ClickGUIModule() {
        super("Click Gui", "Toggles the Volt GUI", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.CLIENT);
        addSettings(scale);
    }

    @Override
    public void onEnable() {
        prevGuiScale = mc.options.getGuiScale().getValue();
        if (mc.currentScreen != null) return;
        switch (scale.getMode()) {
            case "Small" -> mc.options.getGuiScale().setValue(1);
            case "Medium" -> mc.options.getGuiScale().setValue(2);
            case "Large" -> mc.options.getGuiScale().setValue(3);
        }
        mc.setScreen(new ClickGui());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.options.getGuiScale().setValue(prevGuiScale);
        if (mc.currentScreen instanceof ClickGui) {
            mc.setScreen(null);
        }
        super.onDisable();
    }
}
