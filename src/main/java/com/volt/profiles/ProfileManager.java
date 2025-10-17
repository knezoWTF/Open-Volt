package com.volt.profiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.volt.Volt;
import com.volt.module.Module;
import com.volt.module.ModuleManager;
import com.volt.module.setting.*;
import com.volt.utils.mc.ChatUtil;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public final class ProfileManager {

    private final ModuleManager moduleManager = Volt.INSTANCE != null ? Volt.INSTANCE.getModuleManager() : null;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Getter
    private final File profileDir = Volt.mc != null && Volt.mc.runDirectory != null
            ? new File(Volt.mc.runDirectory, "Volt" + File.separator + "profiles")
            : new File("profiles");

    public ProfileManager() {
        createProfileDirectoryIfNeeded();
    }

    private void createProfileDirectoryIfNeeded() {
        if (profileDir != null && !profileDir.exists() && !profileDir.mkdirs()) {
            ChatUtil.addChatMessage("§cFailed to create profile directory: " + profileDir.getAbsolutePath());
        }
    }

    public void loadProfile(final String profileName) {
        if (profileName == null || moduleManager == null) return;
        final File profileFile = new File(profileDir, profileName + ".json");
        if (!profileFile.exists()) {
            ChatUtil.addChatMessage("Profile not found: " + profileName);
            return;
        }
        resetProfile();
        readProfileFromFile(profileFile);
    }

    private void readProfileFromFile(final File profileFile) {
        if (profileFile == null || !profileFile.exists() || moduleManager == null) return;
        try (FileReader reader = new FileReader(profileFile, StandardCharsets.UTF_8)) {
            final JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json != null) {
                for (Object obj : moduleManager.getModules() != null ? moduleManager.getModules() : Collections.emptyList()) {
                    Module module = (Module) obj;
                    if (module != null && json.has(module.getName())) {
                        final JsonObject moduleJson = json.getAsJsonObject(module.getName());
                        if (moduleJson != null) loadModuleSettings(module, moduleJson);
                    }
                }
            }
            ChatUtil.addChatMessage("Profile loaded successfully.");
        } catch (com.google.gson.JsonSyntaxException jse) {
            ChatUtil.addChatMessage("§cFailed to parse profile (invalid JSON): " + profileFile.getName());
            jse.printStackTrace();
        } catch (IOException e) {
            ChatUtil.addChatMessage("§cFailed to load profile: " + e.getMessage());
        }
    }

    private void loadModuleSettings(final Module module, final JsonObject moduleJson) {
        if (module == null || moduleJson == null) return;
        module.setEnabled(moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean());
        module.setKey(moduleJson.has("bind") ? moduleJson.get("bind").getAsInt() : 0);
        if (module.getSettings() != null) {
            for (Setting setting : module.getSettings()) {
                if (setting != null) loadSettingValue(setting, moduleJson);
            }
        }
    }

    private void loadSettingValue(final Setting setting, final JsonObject moduleJson) {
        if (setting == null || moduleJson == null) return;
        final String name = setting.getName();
        if (name == null || !moduleJson.has(name)) return;
        final JsonElement element = moduleJson.get(name);
        if (element == null || element.isJsonNull()) return;

        try {
            switch (setting) {
                case BooleanSetting booleanSetting -> booleanSetting.setValue(element.getAsBoolean());
                case NumberSetting numberSetting -> numberSetting.setValue(element.getAsDouble());
                case ModeSetting modeSetting -> {
                    String mode = element.getAsString();
                    if (mode != null) modeSetting.setMode(mode);
                }
                case KeybindSetting keybindSetting -> keybindSetting.setKeyCode(element.getAsInt());
                case StringSetting stringSetting -> stringSetting.setValue(element.getAsString());
                case ColorSetting colorSetting -> {
                    if (element.isJsonPrimitive()) {
                        if (element.getAsJsonPrimitive().isString()) {
                            String hex = element.getAsString();
                            if (hex != null) {
                                hex = hex.trim();
                                if (hex.startsWith("#")) hex = hex.substring(1);
                                if (hex.length() >= 6) {
                                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                                    if (colorSetting.isHasAlpha() && hex.length() >= 8) {
                                        int a = Integer.parseInt(hex.substring(6, 8), 16);
                                        colorSetting.setValue(r, g, b, a);
                                    } else {
                                        colorSetting.setValue(r, g, b);
                                    }
                                }
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
                }
                default -> ChatUtil.addChatMessage("§cUnknown setting type: " + setting.getClass().getSimpleName());
            }
        } catch (Exception ex) {
            ChatUtil.addChatMessage("§cFailed to load setting: " + name);
        }
    }

    public void saveProfile(final String profileName) {
        saveProfile(profileName, false);
    }

    public void saveProfile(final String profileName, final boolean forceOverride) {
        if (profileName == null) return;
        final File profileFile = new File(profileDir, profileName + ".json");
        if (profileFile.exists() && !forceOverride) {
            ChatUtil.addChatMessage("§eProfile '" + profileName + "' already exists. Use .save <name> -override to overwrite it.");
            return;
        }

        try {
            if (!profileFile.exists() && !profileFile.createNewFile()) {
                ChatUtil.addChatMessage("§cFailed to create profile file: " + profileFile.getAbsolutePath());
                return;
            }
            ChatUtil.addChatMessage(forceOverride ?
                    "§aProfile '" + profileName + "' overridden successfully." :
                    "§aProfile '" + profileName + "' saved successfully.");
        } catch (IOException e) {
            ChatUtil.addChatMessage("§cFailed to save profile: " + e.getMessage());
            return;
        }
        writeProfileToFile(profileFile);
    }

    private void writeProfileToFile(final File profileFile) {
        if (profileFile == null || moduleManager == null) return;
        final JsonObject json = new JsonObject();
        if (moduleManager.getModules() != null) {
            for (Module module : moduleManager.getModules()) {
                if (module == null) continue;
                final JsonObject moduleJson = new JsonObject();
                moduleJson.addProperty("enabled", module.isEnabled());
                moduleJson.addProperty("bind", module.getKey());
                if (module.getSettings() != null) {
                    for (Setting setting : module.getSettings()) {
                        if (setting != null) saveSettingValue(setting, moduleJson);
                    }
                }
                if (module.getName() != null) json.add(module.getName(), moduleJson);
            }
        }
        try (FileWriter writer = new FileWriter(profileFile, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            ChatUtil.addChatMessage("§cFailed to write profile: " + e.getMessage());
        }
    }

    private void saveSettingValue(final Setting setting, final JsonObject moduleJson) {
        if (setting == null || moduleJson == null) return;
        final String name = setting.getName();
        if (name == null) return;
        try {
            switch (setting) {
                case BooleanSetting booleanSetting -> moduleJson.addProperty(name, booleanSetting.getValue());
                case NumberSetting numberSetting -> moduleJson.addProperty(name, numberSetting.getValue());
                case ModeSetting modeSetting -> {
                    String mode = modeSetting.getMode();
                    if (mode != null) moduleJson.addProperty(name, mode);
                }
                case KeybindSetting keybindSetting -> moduleJson.addProperty(name, keybindSetting.getKeyCode());
                case StringSetting stringSetting -> moduleJson.addProperty(name, stringSetting.getValue());
                case ColorSetting colorSetting -> {
                    String hex = String.format("#%02X%02X%02X", colorSetting.getRed(), colorSetting.getGreen(), colorSetting.getBlue());
                    if (colorSetting.isHasAlpha()) hex += String.format("%02X", colorSetting.getAlpha());
                    moduleJson.addProperty(name, hex);
                }
                default -> ChatUtil.addChatMessage("§cUnknown setting type: " + setting.getClass().getSimpleName());
            }
        } catch (Exception e) {
            ChatUtil.addChatMessage("§cFailed to save setting: " + name);
        }
    }

    public void resetProfile() {
        if (moduleManager == null || moduleManager.getModules() == null) return;
        for (Module module : moduleManager.getModules()) {
            if (module == null) continue;
            module.setEnabled(false);
            module.setKey(0);
            if (module.getSettings() != null) {
                for (Setting setting : module.getSettings()) {
                    if (setting != null) resetSettingValue(setting);
                }
            }
        }
    }

    private void resetSettingValue(final Setting setting) {
        if (setting == null) return;
        try {
            if (setting instanceof BooleanSetting booleanSetting) booleanSetting.setValue(false);
            else if (setting instanceof NumberSetting numberSetting) numberSetting.setValue(numberSetting.getMin());
            else if (setting instanceof ModeSetting modeSetting && modeSetting.getModes() != null && !modeSetting.getModes().isEmpty())
                modeSetting.setMode(modeSetting.getModes().getFirst());
        } catch (Exception ignored) {
        }
    }
}
