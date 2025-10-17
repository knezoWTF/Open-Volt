package com.volt.utils.render;

import java.awt.*;

public class ColorUtils {

    /**
     * Interpolates between two colors.
     *
     * @param a The first color.
     * @param b The second color.
     * @param t The factor to interpolate.
     * @return The interpolated color.
     */
    public static Color colorInterpolate(final Color a, final Color b, final double t) {
        return colorInterpolate(a, b, t, t, t, t);
    }

    /**
     * Interpolates between two colors.
     *
     * @param a  The first color.
     * @param b  The second color.
     * @param tR The factor to interpolate the red value.
     * @param tG The factor to interpolate the green value.
     * @param tB The factor to interpolate the blue value.
     * @param tA The factor to interpolate the alpha value.
     * @return The interpolated color.
     */
    public static Color colorInterpolate(final Color a, final Color b, final double tR, final double tG, final double tB, final double tA) {
        return new Color(
                (float) ((a.getRed() + (b.getRed() - a.getRed()) * tR) / 255F),
                (float) ((a.getGreen() + (b.getGreen() - a.getGreen()) * tG) / 255F),
                (float) ((a.getBlue() + (b.getBlue() - a.getBlue()) * tB) / 255F),
                (float) ((a.getAlpha() + (b.getAlpha() - a.getAlpha()) * tA) / 255F)
        );
    }
}
