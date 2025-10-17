package com.volt.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.volt.utils.IMinecraft;
import net.minecraft.command.CommandSource;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PlayerArgumentType implements ArgumentType<String>, IMinecraft {

    public static PlayerArgumentType create() {
        return new PlayerArgumentType();
    }

    public static String get(final CommandContext<?> context) {
        return context.getArgument("name", String.class);
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(Objects.requireNonNull(mc.getNetworkHandler()).getPlayerList().stream().map(playerListEntry -> playerListEntry.getProfile().getName()), builder);
    }
}
