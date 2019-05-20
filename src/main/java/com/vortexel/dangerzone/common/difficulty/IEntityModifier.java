package com.vortexel.dangerzone.common.difficulty;

import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.config.EntityConfig;

import java.util.Random;

public interface IEntityModifier {

    /**
     * This method fires an event and if the event isn't cancelled, applies the amount to the entity based
     * on the supplied {@link ModifierType}.
     * @param modifierType the type of attribute which will be modified
     * @param initialAmount initial modifier amount
     */
    void modify(ModifierType modifierType, double initialAmount);

    IDangerLevel getCapability();

    EntityConfig getEntityConfig();

    Random getRNG();

    double getDifficultyAtLocation();

    String getEntityClassName();
}
