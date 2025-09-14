package com.volt.profiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.volt.Volt;
import com.volt.module.Module;
import com.volt.module.ModuleManager;
import com.volt.module.setting.*;
import com.volt.utils.mc.ChatUtils;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ProfileManager {

    private final ModuleManager moduleManager = Volt.INSTANCE.getModuleManager();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Getter
    private final File profileDir = new File(Volt.mc.runDirectory, "Volt" + File.separator + "profiles");

    public ProfileManager() {
        createProfileDirectoryIfNeeded();
    }

    private void createProfileDirectoryIfNeeded() {
        if (!profileDir.exists() && !profileDir.mkdirs()) {
            ChatUtils.addChatMessage("§cFailed to create profile directory: " + profileDir.getAbsolutePath());
        }
    }

    public void loadProfile(final String profileName) {
        final File profileFile = new File(profileDir, profileName + ".json");
        if (!profileFile.exists()) {
            ChatUtils.addChatMessage("Profile not found: " + profileName);
            return;
        }
        resetProfile();
        readProfileFromFile(profileFile);
    }

    private void readProfileFromFile(final File profileFile) {
        try (FileReader reader = new FileReader(profileFile, StandardCharsets.UTF_8)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);

            if (json == null) {
                ChatUtils.addChatMessage("§eProfile file is empty or invalid: " + profileFile.getName());
                return;
            }

            for (Module module : moduleManager.getModules()) {
                if (json.has(module.getName())) {
                    final JsonObject moduleJson = json.getAsJsonObject(module.getName());
                    loadModuleSettings(module, moduleJson);
                }
            }
            ChatUtils.addChatMessage("Profile loaded successfully.");
        } catch (com.google.gson.JsonSyntaxException jse) {
            ChatUtils.addChatMessage("§cFailed to parse profile (invalid JSON): " + profileFile.getName());
            jse.printStackTrace();
        } catch (IOException e) {
            ChatUtils.addChatMessage("§cFailed to load profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadModuleSettings(final Module module, final JsonObject moduleJson) {
        module.setEnabled(moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean());
        module.setKey(moduleJson.has("bind") ? moduleJson.get("bind").getAsInt() : 0);
        for (Setting setting : module.getSettings()) {
            loadSettingValue(setting, moduleJson);
        }
    }

    private void loadSettingValue(final Setting setting, final JsonObject moduleJson) {
        final String name = setting.getName();
        if (!moduleJson.has(name)) return;
        final JsonElement element = moduleJson.get(name);

        switch (setting) {
            case BooleanSetting booleanSetting -> booleanSetting.setValue(element.getAsBoolean());
            case NumberSetting numberSetting -> numberSetting.setValue(element.getAsDouble());
            case ModeSetting modeSetting -> modeSetting.setMode(element.getAsString());
            case KeybindSetting keybindSetting -> keybindSetting.setKeyCode(element.getAsInt());
            case StringSetting stringSetting -> stringSetting.setValue(element.getAsString());
            case ColorSetting colorSetting -> {
                try {
                    if (element.isJsonPrimitive()) {
                        if (element.getAsJsonPrimitive().isString()) {
                            String hex = element.getAsString().trim();
                            if (hex.startsWith("#")) hex = hex.substring(1);
                            int r = Integer.parseInt(hex.substring(0, 2), 16);
                            int g = Integer.parseInt(hex.substring(2, 4), 16);
                            int b = Integer.parseInt(hex.substring(4, 6), 16);
                            if (colorSetting.isHasAlpha() && hex.length() == 8) {
                                int a = Integer.parseInt(hex.substring(6, 8), 16);
                                colorSetting.setValue(r, g, b, a);
                            } else {
                                colorSetting.setValue(r, g, b);
                            }
                        } else if (element.getAsJsonPrimitive().isNumber()) {
                            int argb = element.getAsInt();
                            int a = (argb >> 24) & 0xFF;
                            int r = (argb >> 16) & 0xFF;
                            int g = (argb >> 8) & 0xFF;
                            int b = argb & 0xFF;
                            if (colorSetting.isHasAlpha()) colorSetting.setValue(r, g, b, a);
                            else colorSetting.setValue(r, g, b);
                        }
                    }
                } catch (Exception ex) {
                    ChatUtils.addChatMessage("§cFailed to load color for setting: " + name);
                }
            }
            default -> ChatUtils.addChatMessage("§cUnknown setting type: " + setting.getClass().getSimpleName());
        }
    }

    public void saveProfile(final String profileName) {
        saveProfile(profileName, false);
    }

    public void saveProfile(final String profileName, final boolean forceOverride) {
        final File profileFile = new File(profileDir, profileName + ".json");
        if (profileFile.exists() && !forceOverride) {
            ChatUtils.addChatMessage("§eProfile '" + profileName + "' already exists. Use .save <name> -override to overwrite it.");
            return;
        }

        try {
            if (!profileFile.exists()) profileFile.createNewFile();
            ChatUtils.addChatMessage(forceOverride ?
                    "§aProfile '" + profileName + "' overridden successfully." :
                    "§aProfile '" + profileName + "' saved successfully.");
        } catch (IOException e) {
            ChatUtils.addChatMessage("§cFailed to save profile: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        writeProfileToFile(profileFile);
    }

    private void writeProfileToFile(final File profileFile) {
        final JsonObject json = new JsonObject();
        for (Module module : moduleManager.getModules()) {
            final JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("bind", module.getKey());
            for (Setting setting : module.getSettings()) saveSettingValue(setting, moduleJson);
            json.add(module.getName(), moduleJson);
        }
        try (FileWriter writer = new FileWriter(profileFile, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            ChatUtils.addChatMessage("§cFailed to write profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSettingValue(final Setting setting, final JsonObject moduleJson) {
        final String name = setting.getName();
        switch (setting) {
            case BooleanSetting booleanSetting -> moduleJson.addProperty(name, booleanSetting.getValue());
            case NumberSetting numberSetting -> moduleJson.addProperty(name, numberSetting.getValue());
            case ModeSetting modeSetting -> moduleJson.addProperty(name, modeSetting.getMode());
            case KeybindSetting keybindSetting -> moduleJson.addProperty(name, keybindSetting.getKeyCode());
            case StringSetting stringSetting -> moduleJson.addProperty(name, stringSetting.getValue());
            case ColorSetting colorSetting -> {
                String hex = String.format("#%02X%02X%02X", colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue());
                if (colorSetting.isHasAlpha()) hex += String.format("%02X", colorSetting.getAlpha());
                moduleJson.addProperty(name, hex);
            }
            default -> ChatUtils.addChatMessage("§cUnknown setting type: " + setting.getClass().getSimpleName());
        }
    }

    public void resetProfile() {
        for (Module module : moduleManager.getModules()) {
            module.setEnabled(false);
            module.setKey(0);
            for (Setting setting : module.getSettings()) resetSettingValue(setting);
        }
    }

    private void resetSettingValue(final Setting setting) {
        if (setting instanceof BooleanSetting booleanSetting) booleanSetting.setValue(false);
        else if (setting instanceof NumberSetting numberSetting) numberSetting.setValue(numberSetting.getMin());
        else if (setting instanceof ModeSetting modeSetting) modeSetting.setMode(modeSetting.getModes().getFirst());
    }
}
