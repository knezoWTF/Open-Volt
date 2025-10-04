package com.volt.module.setting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class KeybindSetting extends Setting {

    private final boolean moduleKey;
    private final int originalKey;
    private int keyCode;
    private boolean listening;

    public KeybindSetting(String name, int key, boolean moduleKey) {
        super(name);
        this.keyCode = key;
        this.originalKey = key;
        this.moduleKey = moduleKey;
    }

    public void toggleListening() {
        this.listening = !listening;
    }
}