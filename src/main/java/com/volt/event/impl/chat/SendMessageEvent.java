package com.volt.event.impl.chat;

import com.volt.event.types.CancellableEvent;

public class SendMessageEvent extends CancellableEvent {
    private final String message;

    public SendMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
