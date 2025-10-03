package com.volt.utils.simulation;

import lombok.experimental.UtilityClass;

import static com.volt.Volt.shouldUseMouseEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@UtilityClass
public final class ClickSimulator {
    public static void leftClick() {
        if (shouldUseMouseEvent) {
            User32.INSTANCE.mouse_event(User32.MOUSEEVENTF_LEFTDOWN, 0, 0, 0, 0);
            User32.INSTANCE.mouse_event(User32.MOUSEEVENTF_LEFTUP, 0, 0, 0, 0);
        } else {
            MinecraftClient mc = MinecraftClient.getInstance();
            KeyBinding attack = mc.options.attackKey;

            InputUtil.Key key = attack.getDefaultKey();

            KeyBinding.setKeyPressed(key, true);
            KeyBinding.onKeyPressed(key);

            new Thread(() -> {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {}
                KeyBinding.setKeyPressed(key, false);
            }).start();

        }
    }
}