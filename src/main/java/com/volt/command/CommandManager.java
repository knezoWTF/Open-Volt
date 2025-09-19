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
                    ChatUtils.addChatMessage("<red>Unknown command: " + command + ". Type .help for available commands.</red>");
        }
    }

    private void handleSaveCommand(final String[] args) {
        if (args.length < 2) {
            ChatUtils.addChatMessage("<red>Usage: .save <profile_name> [-override]</red>");
            return;
        }

        boolean forceOverride = args[args.length - 1].equalsIgnoreCase("-override");
        final int endIndex = forceOverride ? args.length - 1 : args.length;
        final String profileName = String.join(" ", Arrays.copyOfRange(args, 1, endIndex));

        profileManager.saveProfile(profileName, forceOverride);
    }

    private void handleLoadCommand(final String[] args) {
        if (args.length < 2) {
            ChatUtils.addChatMessage("<red>Usage: .load <profile_name></red>");
            return;
        }
        final String profileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        profileManager.loadProfile(profileName);
    }

    private void handleListProfilesCommand() {
        final File profileDir = profileManager.getProfileDir();

        if (!profileDir.exists() || !profileDir.isDirectory()) {
            ChatUtils.addChatMessage("<red>No profiles directory found.</red>");
            return;
        }

        final File[] profiles = profileDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (profiles == null || profiles.length == 0) {
            ChatUtils.addChatMessage("<yellow>No profiles found.</yellow>");
            return;
        }

        ChatUtils.addChatMessage("<aqua>Available profiles:</aqua>");
        for (File profile : profiles) {
            ChatUtils.addChatMessage("<gray>- " + profile.getName().replace(".json", "") + "</gray>");
        }
    }


    private void handleDeleteProfileCommand(final String[] args) {
        if (args.length < 2) {
            ChatUtils.addChatMessage("<red>Usage: .deleteprofile <profile_name></red>");
            return;
        }

        final String profileName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        final File profileFile = new File(profileManager.getProfileDir(), profileName + ".json");

        if (!profileFile.exists()) {
            ChatUtils.addChatMessage("<red>Profile '" + profileName + "' not found.</red>");
            return;
        }

        if (profileFile.delete()) {
            ChatUtils.addChatMessage("<green>Profile '" + profileName + "' deleted successfully!</green>");
        } else {
            ChatUtils.addChatMessage("<red>Failed to delete profile '" + profileName + "'.</red>");
        }
    }
    private void handleHelpCommand() {
        String helpMessage = """
        <aqua>=== Volt Config Commands ===</aqua>
        <gray>.save <name></gray><white> - Save current config as profile</white>
        <gray>.save <name> -override</gray><white> - Override existing profile</white>
        <gray>.load <name></gray><white> - Load a saved profile</white>
        <gray>.profiles</gray><white> - List all saved profiles</white>
        <gray>.deleteprofile <name></gray><white> - Delete a profile</white>
        <gray>.help</gray><white> - Show this help message</white>""";
        ChatUtils.addChatMessage(helpMessage);
    }
}
