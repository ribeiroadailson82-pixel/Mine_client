package com.mineclient.util;

public class TimerUtil {

    private long lastTime = System.currentTimeMillis();

    public boolean hasElapsed(long milliseconds) {
        return System.currentTimeMillis() - lastTime >= milliseconds;
    }

    public long elapsed() {
        return System.currentTimeMillis() - lastTime;
    }

    public void reset() {
        lastTime = System.currentTimeMillis();
    }
}
