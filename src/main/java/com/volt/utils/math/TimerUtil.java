package com.volt.utils.math;

import lombok.Getter;

@Getter
public final class TimerUtil {
    private long lastTime = System.currentTimeMillis();

    public void reset() {
        lastTime = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - lastTime;
    }

    public boolean hasElapsedTime(long delay) {
        return getElapsedTime() >= delay;
    }

    public boolean hasElapsedTime(long delay, boolean reset) {
        if (getElapsedTime() >= delay) {
            if (reset) reset();
            return true;
        }
        return false;
    }

}
