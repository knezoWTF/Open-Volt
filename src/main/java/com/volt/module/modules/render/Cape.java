package com.volt.module.modules.render;

import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.BooleanSetting;
import com.volt.module.setting.ModeSetting;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;

public final class Cape extends Module {
    private final BooleanSetting others = new BooleanSetting("Others", false);
    private final ModeSetting texture;

    private static final String DEFAULT_TEXTURE = "269788df6305de37f5f62f2431b2b0c3.png";
    private static final String ASTOLFO_TEXTURE = "astolfo.png";

    public Cape() {
        super("Cape", "Renders a custom cape", -1, Category.RENDER);
        texture = new ModeSetting("Texture", DEFAULT_TEXTURE, DEFAULT_TEXTURE, ASTOLFO_TEXTURE);
        addSettings(others, texture);
    }

    public boolean shouldRender(AbstractClientPlayerEntity player) {
        if (isNull()) return false;
        if (player == mc.player) return true;
        return others.getValue();
    }

    public Identifier getSelectedTexture() {
        String name = texture.getMode();
        if (name == null || name.isEmpty()) {
            name = DEFAULT_TEXTURE;
        }
        return Identifier.of("volt", "capes/" + name);
    }
}
