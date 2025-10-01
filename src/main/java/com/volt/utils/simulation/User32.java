package com.volt.utils.simulation;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface User32 extends Library {
    User32 INSTANCE = Native.load("user32", User32.class);

    void mouse_event(int dwFlags, int dx, int dy, int dwData, int dwExtraInfo);

    int MOUSEEVENTF_LEFTDOWN = 0x0002;
    int MOUSEEVENTF_LEFTUP   = 0x0004;
}

