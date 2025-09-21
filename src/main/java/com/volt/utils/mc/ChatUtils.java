package com.volt.utils.mc;

import com.volt.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

@UtilityClass
public final class ChatUtils implements IMinecraft {

    public static void addChatMessage(String message) {
        if (mc.player == null || mc.world == null || mc.inGameHud == null || mc.inGameHud.getChatHud() == null) {
            System.out.println("[Volt] " + message);
            return;
        }

        Component component = MiniMessage.miniMessage().deserialize(message == null ? "" : message);
        Text text = toMcText(component);
        mc.inGameHud.getChatHud().addMessage(text);
    }

    private static Text toMcText(Component component) {
        try {
            String json = GsonComponentSerializer.gson().serialize(component);
            assert mc.world != null;
            Text text = Text.Serialization.fromJson(json, mc.world.getRegistryManager());
            if (text != null) return text;
        } catch (Exception ignored) {
        }
        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        return Text.of(plain);
    }
}
