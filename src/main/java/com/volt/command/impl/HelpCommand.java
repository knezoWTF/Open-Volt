package com.volt.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.volt.Volt;
import com.volt.command.Command;
import com.volt.command.CommandManager;
import com.volt.utils.mc.ChatUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HelpCommand extends Command {

    private int maxCommandsPerPage = 5;

    public HelpCommand() {
        super("Shows information about all commands", "help", "commands", "commandlist", "?");
    }

    @Override
    public void build(final LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> this.helpMenu(1));
        builder.then(argument("page", IntegerArgumentType.integer(0)).executes(context -> this.helpMenu(IntegerArgumentType.getInteger(context, "page"))));
    }

    private int helpMenu(final int pageInput) {
        final int totalCommands = Volt.INSTANCE.getCommandManager().list().size();

        int maxPages = (int) Math.ceil((double) totalCommands / this.maxCommandsPerPage) - 1;
        int page = Math.max(0, Math.min(pageInput - 1, maxPages));

        ChatUtil.emptyChatMessage(false);
        ChatUtil.chatMessage(
                Text.literal(
                        Formatting.DARK_GRAY + "[" + Formatting.GOLD + "Page " +
                                Formatting.DARK_AQUA + (page + 1) + Formatting.GRAY + " / " + Formatting.DARK_AQUA + (maxPages + 1) +
                                Formatting.DARK_GRAY + " | " + Formatting.GOLD + "Commands" + Formatting.GRAY + ": " + Formatting.DARK_AQUA + totalCommands + Formatting.DARK_GRAY + "]"
                )
        );
        ChatUtil.emptyChatMessage(false);

        final String commandPrefix = CommandManager.COMMAND_PREFIX;
        for (int i = page * this.maxCommandsPerPage; i < Math.min((page + 1) * this.maxCommandsPerPage, totalCommands); i++) {
            final Command command = Volt.INSTANCE.getCommandManager().list().get(i);
            final MutableText commandText = Text.literal(commandPrefix + String.join(" | ", command.aliases()));
            commandText.styled(style ->
                    style.withClickEvent(new ClickEvent(
                            ClickEvent.Action.SUGGEST_COMMAND,
                            commandPrefix + command.aliases()[0]
                    )).withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text.literal("Insert the command into the chat field")
                    ))
            );
            commandText.append(Text.literal(" » ").formatted(Formatting.DARK_GRAY));
            String description = command.description();
            if (Objects.isNull(description) || description.isEmpty()) {
                description = "No description available.";
            }
            commandText.append(Text.literal(description).formatted(Formatting.GRAY));
            ChatUtil.chatMessage(commandText);
        }

        page++;
        maxPages += 2;

        final int prevPage = Math.max(1, page - 1);
        final int nextPage = Math.min(maxPages, page + 1);
        final MutableText buttons = Text.literal("");
        if (prevPage != page) {
            final MutableText prevPageButton = Text.literal("<< Previous Page")
                    .formatted(Formatting.RED)
                    .styled(style -> style
                            .withClickEvent(Volt.INSTANCE.getCommandManager().generateClickEvent("help " + prevPage))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT, Text.literal("Go to page " + prevPage))
                            )
                    );
            buttons.append(prevPageButton);
            if (nextPage < maxPages) {
                buttons.append(Text.literal(" | ").formatted(Formatting.GRAY));
            }
        }
        if (nextPage < maxPages) {
            final MutableText nextPageButton = Text.literal("Next Page >>")
                    .formatted(Formatting.GREEN)
                    .styled(style -> style
                            .withClickEvent(Volt.INSTANCE.getCommandManager().generateClickEvent("help " + nextPage))
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT, Text.literal("Go to page " + nextPage))
                            )
                    );
            buttons.append(nextPageButton);
        }
        ChatUtil.emptyChatMessage(false);
        ChatUtil.chatMessage(buttons);
        ChatUtil.emptyChatMessage(false);



        return SINGLE_SUCCESS;
    }
}
