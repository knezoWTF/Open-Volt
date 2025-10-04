package com.volt.gui.utils;

import com.volt.module.Module;
import com.volt.module.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public class SearchUtils {

    public static List<Module> filterModulesBySearch(List<Module> modules, String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return modules;
        }

        List<Module> filteredModules = new ArrayList<>();
        String query = searchQuery.toLowerCase();

        for (Module module : modules) {
            if (module.getName().toLowerCase().contains(query)) {
                filteredModules.add(module);
                continue;
            }

            if (module.getDescription() != null && module.getDescription().toLowerCase().contains(query)) {
                filteredModules.add(module);
                continue;
            }

            if (module.getSettings() != null) {
                for (Setting setting : module.getSettings()) {
                    if (setting.getName().toLowerCase().contains(query)) {
                        filteredModules.add(module);
                        break;
                    }
                }
            }
        }

        return filteredModules;
    }

    public static String truncateText(String text, float maxWidth, float charWidth) {
        float textWidth = text.length() * charWidth;
        if (textWidth <= maxWidth) {
            return text;
        }

        String truncated = text;
        while (textWidth > maxWidth && truncated.length() > 1) {
            truncated = truncated.substring(0, truncated.length() - 1);
            textWidth = truncated.length() * charWidth;
        }

        return truncated.isEmpty() ? "" : truncated + "...";
    }

    public static String clipTextToWidth(String text, float maxWidth, float charWidth) {
        if (text.length() * charWidth <= maxWidth) {
            return text;
        }

        int maxChars = (int) (maxWidth / charWidth);
        if (maxChars <= 3) {
            return "...";
        }

        return text.substring(0, maxChars - 3) + "...";
    }
}