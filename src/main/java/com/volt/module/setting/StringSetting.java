package com.volt.module.setting;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StringSetting extends Setting {
    private String value;

    public StringSetting(String name, String value) {
        super(name);
        this.value = value;
    }
}