package com.volt.module;

import lombok.Getter;

@Getter
public enum Category {
    COMBAT("Combat"),
    PLAYER("Player"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    MISC("Misc"),
    CLIENT("Client"),
    CONFIG("Config");

    private final String name;

    Category(String name) {
        this.name = name;
    }
}
