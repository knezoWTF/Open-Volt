package com.volt.event.impl.render;

import com.volt.event.types.Event;
import net.minecraft.client.gui.DrawContext;

public class EventRender2D implements Event {
    private DrawContext context;
    private int width;
    private int height;

    public EventRender2D(DrawContext context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public DrawContext getContext() {
        return context;
    }

    public void setContext(DrawContext context) {
        this.context = context;
    }
}
