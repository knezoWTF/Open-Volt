package com.volt.module.modules.render;

import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.NumberSetting;

public final class SwingSpeed extends Module {
    public static final NumberSetting swingSpeed = new NumberSetting("Swing Speed", 1, 20, 12, 1);

    public SwingSpeed() {
        super("Swing Speed", "Modifies your swing speed", -1, Category.RENDER);
        this.addSetting(swingSpeed);
    }

    public int getSwingSpeed() {
        return swingSpeed.getValueInt();
    }
}
