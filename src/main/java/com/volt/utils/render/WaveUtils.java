package com.volt.utils.render;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class WaveUtils {
    public static float getTime() {
        return (System.currentTimeMillis() % 60000) / 1000.0f;
    }
} 