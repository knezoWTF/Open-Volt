package com.volt.event.impl.chat;

import com.volt.event.types.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatEvent extends CancellableEvent {
    private String message;

    public ChatEvent(String message) {
        this.message = message;
    }
}