package com.volt.event.impl.network;

import com.volt.event.types.CancellableEvent;
import com.volt.event.types.TransferOrder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.Packet;


@Getter
@Setter
public class EventPacket extends CancellableEvent {
    private final TransferOrder order;
    private Packet packet;

    public EventPacket(Packet packet, TransferOrder order) {
        this.packet = packet;
        this.order = order;
    }

}