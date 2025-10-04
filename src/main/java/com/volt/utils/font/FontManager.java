package com.volt.utils.font;


import com.volt.Volt;
import com.volt.utils.font.fonts.FontRenderer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FontManager {

    private final Map<FontKey, FontRenderer> fontCache = new HashMap<>();

    public void initialize() {
        for (Type type : Type.values()) {
            for (int size = 4; size <= 32; size++) {
                fontCache.put(new FontKey(size, type), create(size, type.getType()));
            }
        }
    }

    @SneakyThrows
    public FontRenderer create(float size, String name) {
        String path = "volt/fonts/" + name + ".ttf";

        try (InputStream inputStream = Volt.class.getClassLoader().getResourceAsStream(path)) {
            Font[] font = Font.createFonts(Objects.requireNonNull(inputStream));

            return new FontRenderer(font, size, 256, 2);
        }
    }

    public FontRenderer getSize(int size, Type type) {
        return fontCache.computeIfAbsent(new FontKey(size, type), k -> create(size, type.getType()));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        Inter("Inter"),
        JetbrainsMono("JetbrainsMono"),
        Poppins("Poppins-Medium");

        private final String type;
    }

    private record FontKey(int size, Type type) {

        @Override
            public @NotNull String toString() {
                return "FontKey[" +
                        "size=" + size + ", " +
                        "type=" + type + ']';
            }

        }
}
