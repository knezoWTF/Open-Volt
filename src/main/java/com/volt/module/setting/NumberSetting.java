package com.volt.module.setting;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NumberSetting extends Setting {
    private final double min;
    private final double max;
    private final double increment;
    private double value;

    public NumberSetting(String name, double min, double max, double value, double increment) {
        super(name);
        this.min = min;
        this.max = max;
        this.increment = increment;
        setValue(value);
    }

    public void setValue(double value) {
        double precision = 1.0D / increment;
        this.value = Math.round(Math.max(min, Math.min(max, value)) * precision) / precision;
    }

    public int getValueInt() {
        return (int) value;
    }

    public float getValueFloat() {
        return (float) value;
    }
}
