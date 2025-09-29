package com.volt.module.modules.render;

import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import net.minecraft.client.network.AbstractClientPlayerEntity;

public final class Cape extends Module {
    private final BooleanSetting others = new BooleanSetting("Others", false);

    public Cape() {
        super("Cape", "Renders a custom cape", -1, Category.RENDER);
        addSettings(others);
    }

    public boolean shouldRender(AbstractClientPlayerEntity player) {
        if (isNull()) return false;
        if (player == mc.player) return true;
        return others.getValue();
    }
}
