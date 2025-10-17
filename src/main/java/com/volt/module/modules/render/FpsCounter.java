package com.volt.module.modules.render;

import com.volt.Volt;
import com.volt.event.impl.render.EventRender2D;
import com.volt.module.Category;
import com.volt.module.Module;
import com.volt.module.setting.NumberSetting;
import com.volt.utils.render.font.FontManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;

import java.awt.*;

public class FpsCounter extends Module {
    public static final NumberSetting x = new NumberSetting("X Position", 0, 1920, 10, 1);
    public static final NumberSetting y = new NumberSetting("Y Position", 0, 1080, 10, 1);
    public static final NumberSetting backgroundOpacity = new NumberSetting("Background Opacity", 0, 255, 120, 1);
    public static final NumberSetting cornerRadius = new NumberSetting("Corner Radius", 0, 20, 6, 1);

    public FpsCounter() {
        super("FPS Counter", "Displays FPS with rounded background", -1, Category.RENDER);
        addSettings(x, y, backgroundOpacity, cornerRadius);
    }

    @EventHandler
    private void onEventRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        String fpsText = "FPS: " + MinecraftClient.getInstance().getCurrentFps();


        var fontRenderer = Volt.INSTANCE.fontManager.getSize(14, FontManager.Type.Inter);

        int padding = 6;

        int posX = x.getValueInt();
        int posY = y.getValueInt();


        fontRenderer.drawString(
                event.getContext().getMatrices(),
                fpsText,
                posX + padding,
                posY + padding,
                Color.WHITE
        );
    }
}