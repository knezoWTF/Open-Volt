package com.volt.utils.render.font.fonts;

import org.jetbrains.annotations.NotNull;

record Glyph(int u, int v, int width, int height, char value, GlyphMap owner) {

    @Override
    public @NotNull String toString() {
        return "Glyph[" +
                "u=" + u + ", " +
                "v=" + v + ", " +
                "width=" + width + ", " +
                "height=" + height + ", " +
                "value=" + value + ", " +
                "owner=" + owner + ']';
    }

}
