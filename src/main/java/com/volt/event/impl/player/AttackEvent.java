package com.volt.event.impl.player;

import com.volt.event.types.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@AllArgsConstructor
@Getter
public class AttackEvent extends CancellableEvent {
    Entity target;
}
