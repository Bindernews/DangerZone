package com.vortexel.dangerzone.common.difficulty;

import lombok.*;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This event is fired whenever a modifier is going to be applied to an {@code Entity}.
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Cancelable
public class ModifierAppliedEvent extends Event {

    public final Entity entity;
    public final ModifierType modifier;
    public double amount;
}
