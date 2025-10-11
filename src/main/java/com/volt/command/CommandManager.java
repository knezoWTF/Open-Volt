package com.volt.command;

import com.volt.Volt;
import com.volt.event.impl.chat.ChatEvent;
import com.volt.profiles.ProfileManager;
import com.volt.utils.mc.ChatUtils;
import meteordevelopment.orbit.EventHandler;

import java.io.File;
import java.util.Arrays;

public class CommandManager {
    private static final String PREFIX = ".";
    private final ProfileManager profileManager;

    public CommandManager() {
        this.profileManager = Volt.INSTANCE.getProfileManager();
        Volt.INSTANCE.getVoltEventBus().subscribe(this);
    }

    @EventHandler
    public void onChat(final ChatEvent event) {
        final String message = event.getMessage();
        if (!message.startsWith(PREFIX)) return;

        event.setCancelled(true);
        final String[] args = message.substring(PREFIX.length()).split(" ");
        final String command = args[0].toLowerCase();

        switch (command) {
            case "save", "saveconfig" -> handleSaveCommand(args);
            case "load", "loadconfig" -> handleLoadCommand(args);
            case "profiles", "listprofiles" -> handleListProfilesCommand();
            case "deleteprofile" -> handleDeleteProfileCommand(args);
            case "help", "commands" -> handleHelpCommand();
            default ->
                    ChatUtils.addChatMessage("§cUnknown command: " + command + ". Type .help for available commands.");
        }
    }

    private void handleSaveCommand(final String[] args) {
        if (args.length < 2) {
            ChatUtils.addChatMessage("§cUsage: .save <profile_name> [-override]");
            return;
        }

        boolean forceOverride = args[args.length - 1].equalsIgnoreCase("-override");
        final int endIndex = forceOverride ? args.length - 1 : args.length;
        final String profileName = String.join(" ", Arrays.copyOfRange(args, 1, endIndex));

        profileManager.saveProfile(profileName, forceOverride);
    }

    private void handleLoadCommand(final String[] args) {
        if (args.length < 2) {
            ChatUtils.addChatMessage("§cUsage: .load <profile_name>");
            return;
        }
        final String profileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        profileManager.loadProfile(profileName);
    }

    private void handleListProfilesCommand() {
        final File profileDir = profileManager.getProfileDir();

        if (!profileDir.exists() || !profileDir.isDirectory()) {
            ChatUtils.addChatMessage("§cNo profiles directory found.");
            return;
        }

        final File[] profiles = profileDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (profiles == null || profiles.length == 0) {
            ChatUtils.addChatMessage("§eNo profiles found.");
            return;
        }

        ChatUtils.addChatMessage("§bAvailable profiles:");
        for (File profile : profiles) {
            ChatUtils.addChatMessage("§7- " + profile.getName().replace(".json", ""));
        }
    }


    private void handleDeleteProfileCommand(final String[] args) {
        if (args.length < 2) {
            ChatUtils.addChatMessage("§cUsage: .deleteprofile <profile_name>");
            return;
        }

        final String profileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        final File profileFile = new File(profileManager.getProfileDir(), profileName + ".json");

        if (!profileFile.exists()) {
            ChatUtils.addChatMessage("§cProfile '" + profileName + "' not found.");
            return;
        }

        if (profileFile.delete()) {
            ChatUtils.addChatMessage("§aProfile '" + profileName + "' deleted successfully!");
        } else {
            ChatUtils.addChatMessage("§cFailed to delete profile '" + profileName + "'.");
        }
    }

    private void handleHelpCommand() {
        ChatUtils.addChatMessage("§b=== Volt Config Commands ===");
        ChatUtils.addChatMessage("§7.save <name> §f- Save current config as profile");
        ChatUtils.addChatMessage("§7.save <name> -override §f- Override existing profile");
        ChatUtils.addChatMessage("§7.load <name> §f- Load a saved profile");
        ChatUtils.addChatMessage("§7.profiles §f- List all saved profiles");
        ChatUtils.addChatMessage("§7.deleteprofile <name> §f- Delete a profile");
        ChatUtils.addChatMessage("§7.help §f- Show this help message");
    }
}
