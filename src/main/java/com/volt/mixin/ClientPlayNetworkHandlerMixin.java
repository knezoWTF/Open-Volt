package com.volt.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.volt.Volt;
import com.volt.command.Command;
import com.volt.command.CommandManager;
import com.volt.event.impl.chat.SendMessageEvent;
import com.volt.utils.mc.ChatUtil;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.volt.Volt.mc;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        final String prefix = CommandManager.COMMAND_PREFIX;
        if (message.startsWith(prefix)) {
            try {
                Volt.INSTANCE.getCommandManager().commandDispatcher().execute(message.substring(prefix.length()), Command.COMMAND_SOURCE);
            } catch (final CommandSyntaxException exception) {
                ChatUtil.errorChatMessage(exception.getMessage());
            }

            mc.inGameHud.getChatHud().addToMessageHistory(message);
            ci.cancel();
        }

        final SendMessageEvent event = new SendMessageEvent(message);
        Volt.INSTANCE.getVoltEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
