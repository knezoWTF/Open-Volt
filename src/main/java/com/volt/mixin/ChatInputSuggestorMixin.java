package com.volt.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.volt.Volt;
import com.volt.command.Command;
import com.volt.command.CommandManager;
import com.volt.module.modules.client.Client;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {

    @Shadow
    @Nullable
    private ParseResults<CommandSource> parse;

    @Shadow
    @Final
    TextFieldWidget textField;

    @Shadow
    @Nullable
    private ChatInputSuggestor.SuggestionWindow window;

    @Shadow
    boolean completingSuggestions;

    @Shadow
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    protected abstract void showCommandSuggestions();

    @Inject(
            method = "refresh",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/StringReader;canRead()Z",
                    remap = false
            ),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void suggestClientCommands(final CallbackInfo callback, final String string, final StringReader reader) {
        final CommandManager commandManager = Volt.INSTANCE.getCommandManager();
        final String prefix = CommandManager.COMMAND_PREFIX;
        final int length = prefix.length();

        if (reader.canRead(length) && reader.getString().startsWith(prefix, reader.getCursor())) {
            reader.setCursor(reader.getCursor() + length);

            if (Objects.isNull(this.parse)) {
                this.parse = commandManager.commandDispatcher().parse(reader, Command.COMMAND_SOURCE);
            }

            final boolean shouldCancel = Volt.INSTANCE.getModuleManager().getModule(Client.class)
                    .filter(module -> !module.isEnabled() && !module.showCommandSuggestions())
                    .isPresent();

            if (shouldCancel) {
                callback.cancel();
                return;
            }

            final int cursor = this.textField.getCursor();
            if (cursor >= length && (Objects.isNull(this.window) || !this.completingSuggestions)) {
                this.pendingSuggestions = commandManager.commandDispatcher().getCompletionSuggestions(this.parse, cursor);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.showCommandSuggestions();
                    }
                });
            }

            callback.cancel();
        }
    }
}
