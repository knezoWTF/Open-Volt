package com.volt.utils.math;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

@UtilityClass
public final class MathUtils {
    private final SecureRandom random = new SecureRandom();

    public static double randomDoubleBetween(double origin, double bound) {
        if (origin >= bound) {
            origin--;
        }
        return random.nextDouble(origin, bound);
    }
}