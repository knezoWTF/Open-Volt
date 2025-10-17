package com.volt.utils.math;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public final class MathUtils {
    private final Random random = new Random();

    public static double randomDoubleBetween(double origin, double bound) {
        return random.nextDouble(Math.clamp(origin, Integer.MIN_VALUE, bound - 1), bound);
    }
}