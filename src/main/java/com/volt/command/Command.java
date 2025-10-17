package com.volt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.volt.utils.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.command.CommandManager;

public abstract class Command implements IMinecraft {

    private final String description;
    private final String[] aliases;

    public static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
    public static final CommandSource COMMAND_SOURCE = new ClientCommandSource(null, MinecraftClient.getInstance());

    public Command(final String description, final String... aliases) {
        this.description = description;
        this.aliases = aliases;
    }

    public abstract void build(final LiteralArgumentBuilder<CommandSource> builder);

    public void publish(final CommandDispatcher<CommandSource> dispatcher) {
        for (final String alias : this.aliases) {
            final LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(alias);
            this.build(builder);
            dispatcher.register(builder);
        }
    }

    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public String name() {
        return this.aliases[0];
    }

    public String description() {
        return this.description;
    }

    public String[] aliases() {
        return this.aliases;
    }
}
