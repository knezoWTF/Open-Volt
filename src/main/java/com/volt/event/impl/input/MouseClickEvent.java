package com.volt.event.impl.input;

import com.volt.event.types.Event;

public record MouseClickEvent(int button, int action, int mods) implements Event {

}