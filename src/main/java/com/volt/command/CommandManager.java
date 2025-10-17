package com.volt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.volt.command.impl.ChatClearCommand;
import com.volt.command.impl.HelpCommand;
import com.volt.command.impl.ProfileCommand;
import com.volt.structure.Registry;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;

import java.util.UUID;

public class CommandManager extends Registry<Command> {

    private final CommandDispatcher<CommandSource> commandDispatcher = new CommandDispatcher<>();

    public static final String COMMAND_SECRET = UUID.randomUUID().toString();
    public static final String COMMAND_PREFIX = ".";

    public CommandManager() {
        this.addConsumer(command -> command.publish(this.commandDispatcher));
    }

    @Override
    public void init() {
        this.add(
                new HelpCommand(),
                new ChatClearCommand(),
                new ProfileCommand()
        );
    }

    public ClickEvent generateClickEvent(final String command) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, COMMAND_SECRET + command);
    }

    public CommandDispatcher<CommandSource> commandDispatcher() {
        return this.commandDispatcher;
    }
}
