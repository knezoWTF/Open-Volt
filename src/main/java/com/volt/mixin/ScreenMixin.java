package com.volt.mixin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.volt.Volt;
import com.volt.command.Command;
import com.volt.command.CommandManager;
import com.volt.gui.ClickGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow
    @Nullable
    protected MinecraftClient client;

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void renderBackgroundInject(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (client == null) return;
        if (client.currentScreen instanceof ClickGui) {
            ci.cancel();
        }
    }

    @Inject(method = "handleTextClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringHelper;stripInvalidChars(Ljava/lang/String;)Ljava/lang/String;", ordinal = 1), cancellable = true)
    private void executeClientCommands(final Style style, final CallbackInfoReturnable<Boolean> cir) {
        final ClickEvent clickEvent = style.getClickEvent();
        if (Objects.nonNull(clickEvent)) {
            if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                final String value = clickEvent.getValue(), secret = CommandManager.COMMAND_SECRET;
                if (value.startsWith(secret) && MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
                    try {
                        Volt.INSTANCE.getCommandManager().commandDispatcher().execute(value.replaceFirst(secret, ""), Command.COMMAND_SOURCE);
                        cir.setReturnValue(true);
                    } catch (final CommandSyntaxException exception) {
                        Volt.INSTANCE.getLogger().error("Failed to run command.", exception);
                    }
                }
            }
        }
    }
}
