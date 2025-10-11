package com.volt.module.setting;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    private final String defaultMode;
    @Getter
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.defaultMode = defaultMode;
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
        if (this.index == -1) {
            this.index = 0;
        }
    }

    public String getMode() {
        if (index < 0 || index >= modes.size()) {
            index = 0;
        }
        return modes.get(index);
    }

    public void setMode(String mode) {
        int newIndex = modes.indexOf(mode);
        if (newIndex != -1) {
            index = newIndex;
        }
    }

    public void cycle() {
        index = (index + 1) % modes.size();
    }

    public boolean isMode(String mode) {
        return mode.equals(getMode());
    }
}
