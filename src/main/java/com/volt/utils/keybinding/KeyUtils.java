package com.volt.utils.keybinding;

import com.volt.IMinecraft;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

@UtilityClass
public final class KeyUtils implements IMinecraft {

    public static String getKey(int key) {
        return switch (key) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "LMB";
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "RMB";
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "MMB";
            case GLFW.GLFW_MOUSE_BUTTON_4 -> "Mouse 4";
            case GLFW.GLFW_MOUSE_BUTTON_5 -> "Mouse 5";
            case GLFW.GLFW_MOUSE_BUTTON_6 -> "Mouse 6";
            case GLFW.GLFW_MOUSE_BUTTON_7 -> "Mouse 7";
            case GLFW.GLFW_MOUSE_BUTTON_8 -> "Mouse 8";
            case GLFW.GLFW_KEY_UNKNOWN -> "Unknown";
            case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "Grave Accent";
            case GLFW.GLFW_KEY_WORLD_1 -> "World 1";
            case GLFW.GLFW_KEY_WORLD_2 -> "World 2";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "Print Screen";
            case GLFW.GLFW_KEY_PAUSE -> "Pause";
            case GLFW.GLFW_KEY_INSERT -> "Insert";
            case GLFW.GLFW_KEY_DELETE -> "Delete";
            case GLFW.GLFW_KEY_HOME -> "Home";
            case GLFW.GLFW_KEY_PAGE_UP -> "Page Up";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "Page Down";
            case GLFW.GLFW_KEY_END -> "End";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "Left Control";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "Right Control";
            case GLFW.GLFW_KEY_LEFT_ALT -> "Left Alt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "Right Alt";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "Left Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "Right Shift";
            case GLFW.GLFW_KEY_UP -> "Arrow Up";
            case GLFW.GLFW_KEY_DOWN -> "Arrow Down";
            case GLFW.GLFW_KEY_LEFT -> "Arrow Left";
            case GLFW.GLFW_KEY_RIGHT -> "Arrow Right";
            case GLFW.GLFW_KEY_APOSTROPHE -> "Apostrophe";
            case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock";
            case GLFW.GLFW_KEY_MENU -> "Menu";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "Left Super";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "Right Super";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_KP_ENTER -> "Numpad Enter";
            case GLFW.GLFW_KEY_NUM_LOCK -> "Num Lock";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_F13 -> "F13";
            case GLFW.GLFW_KEY_F14 -> "F14";
            case GLFW.GLFW_KEY_F15 -> "F15";
            case GLFW.GLFW_KEY_F16 -> "F16";
            case GLFW.GLFW_KEY_F17 -> "F17";
            case GLFW.GLFW_KEY_F18 -> "F18";
            case GLFW.GLFW_KEY_F19 -> "F19";
            case GLFW.GLFW_KEY_F20 -> "F20";
            case GLFW.GLFW_KEY_F21 -> "F21";
            case GLFW.GLFW_KEY_F22 -> "F22";
            case GLFW.GLFW_KEY_F23 -> "F23";
            case GLFW.GLFW_KEY_F24 -> "F24";
            case GLFW.GLFW_KEY_F25 -> "F25";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "Scroll Lock";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "Left Bracket";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "Right Bracket";
            case GLFW.GLFW_KEY_SEMICOLON -> "Semicolon";
            case GLFW.GLFW_KEY_EQUAL -> "Equals";
            case GLFW.GLFW_KEY_BACKSLASH -> "Backslash";
            case GLFW.GLFW_KEY_COMMA -> "Comma";
            default -> {
                String keyName = GLFW.glfwGetKeyName(key, 0);
                yield keyName == null ? "None" : StringUtils.capitalize(keyName);
            }
        };
    }

    public static boolean isKeyPressed(int keyCode) {
        if (keyCode <= 8)
            return GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;

        return GLFW.glfwGetKey(mc.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
    }
}