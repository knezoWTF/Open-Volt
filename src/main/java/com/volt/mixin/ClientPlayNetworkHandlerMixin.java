package com.volt.mixin;

import com.volt.Volt;
import com.volt.event.impl.chat.SendMessageEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        SendMessageEvent event = new SendMessageEvent(message);
        Volt.INSTANCE.getVoltEventBus().post(event);

        if (event.isCancelled()) ci.cancel();
    }
}
