package com.volt.event.impl.world;

import com.volt.event.types.Event;
import lombok.Getter;
import net.minecraft.client.world.ClientWorld;

@Getter
public class WorldChangeEvent implements Event {
    ClientWorld world;

    public WorldChangeEvent(ClientWorld world) {
        this.world = world;
    }
}
