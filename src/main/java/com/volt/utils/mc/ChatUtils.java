package com.volt.utils.mc;

import com.volt.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.text.Text;

@UtilityClass
public final class ChatUtils implements IMinecraft {
    public static void addChatMessage(String text) {
        if (mc.player == null || mc.world == null || mc.inGameHud == null || mc.inGameHud.getChatHud() == null) {
            System.out.println("[Volt] " + text);
            return;
        }
        mc.inGameHud.getChatHud().addMessage(Text.of(text));
    }

}
