package com.volt.command.impl;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.volt.Volt;
import com.volt.command.Command;
import net.minecraft.command.CommandSource;

import java.io.File;
import java.io.IOException;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChatClearCommand extends Command {

    public ChatClearCommand() {
        super("Clears your clientside chat (also allows you to clear your sent history).", "chatclear", "clearchat", "cc");
    }

    @Override
    public void build(final LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            mc.inGameHud.getChatHud().clear(false);
            return SINGLE_SUCCESS;
        });

        builder.then(argument("clear-sent-history", BoolArgumentType.bool()).executes(context -> {
            final boolean clearSentHistory = BoolArgumentType.getBool(context, "clear-sent-history");
            if (clearSentHistory) {
                mc.getCommandHistoryManager().getHistory().clear();
                final File commandHistoryFile = new File(mc.runDirectory, "command_history.txt");
                if (commandHistoryFile.exists()) {
                    if (!commandHistoryFile.delete()) {
                        Volt.INSTANCE.getLogger().error("Failed to delete command history file.");
                    } else {
                        try {
                            if (!commandHistoryFile.createNewFile()) {
                                Volt.INSTANCE.getLogger().error("Failed to create command history file.");
                            }
                        } catch (final IOException exception) {
                            Volt.INSTANCE.getLogger().error("Failed to create command history file.", exception);
                        }
                    }
                }
            }
            mc.inGameHud.getChatHud().clear(clearSentHistory);
            return SINGLE_SUCCESS;
        }));
    }
}
