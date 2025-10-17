package com.volt.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.volt.Volt;
import com.volt.command.Command;
import com.volt.profiles.ProfileManager;
import com.volt.utils.mc.ChatUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

/**
 * TODO: Add click events to it, so users can click on profile names to load/delete them.
 *       Use suggestor for profile names when loading/deleting.
 */
public class ProfileCommand extends Command {

    private final ProfileManager profileManager;

    public ProfileCommand() {
        super("Manages profiles", "profile", "profiles");

        this.profileManager = Volt.INSTANCE.getProfileManager();
    }

    @Override
    public void build(final LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("save")
                .then(argument("name", StringArgumentType.greedyString())
                        .executes(context -> {
                            final String input = StringArgumentType.getString(context, "name");
                            final boolean forceOverride = input.endsWith("-override");
                            final String profileName = forceOverride ? input.substring(0, input.length() - 9).trim() : input;
                            this.profileManager.saveProfile(profileName, forceOverride);
                            return SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("load")
                .then(argument("name", StringArgumentType.greedyString())
                        .executes(context -> {
                            final String profileName = StringArgumentType.getString(context, "name");
                            this.profileManager.loadProfile(profileName);
                            return SINGLE_SUCCESS;
                        })
                )
        );

        builder.then(literal("list")
                .executes(context -> {
                    this.handleListProfiles();
                    return SINGLE_SUCCESS;
                })
        );
        
        builder.then(literal("delete")
                .then(argument("name", StringArgumentType.greedyString())
                        .executes(context -> {
                            final String profileName = StringArgumentType.getString(context, "name");
                            this.handleDeleteProfile(profileName);
                            return SINGLE_SUCCESS;
                        })
                )
        );
    }

    private void handleListProfiles() {
        final File profileDir = this.profileManager.getProfileDir();

        if (!profileDir.exists() || !profileDir.isDirectory()) {
            ChatUtil.errorChatMessage("No profiles directory found.");
            return;
        }

        final File[] profiles = profileDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (profiles == null || profiles.length == 0) {
            ChatUtil.warningChatMessage("No profiles found.");
            return;
        }

        ChatUtil.infoChatMessage(Text.literal("Available profiles:").formatted(Formatting.AQUA));
        for (File profile : profiles) {
            ChatUtil.infoChatMessage(Text.literal("- " + profile.getName().replace(".json", "")).formatted(Formatting.GRAY));
        }
    }

    private void handleDeleteProfile(final String profileName) {
        final File profileFile = new File(this.profileManager.getProfileDir(), profileName + ".json");

        if (!profileFile.exists()) {
            ChatUtil.errorChatMessage("Profile '" + profileName + "' not found.");
            return;
        }

        if (profileFile.delete()) {
            ChatUtil.infoChatMessage("Profile '" + profileName + "' deleted successfully!");
        } else {
            ChatUtil.errorChatMessage("Failed to delete profile '" + profileName + "'.");
        }
    }
}
