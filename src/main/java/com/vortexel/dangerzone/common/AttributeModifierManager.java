package com.vortexel.dangerzone.common;

import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.Map;

public class AttributeModifierManager {

    private Map<Double, Map<Integer, AttributeModifier>> theMap;

    public AttributeModifierManager() {
        theMap = Maps.newHashMap();
    }

    public AttributeModifier get(double amount, int operation) {
        theMap.putIfAbsent(amount, Maps.newHashMap());
        Map<Integer, AttributeModifier> subMap = theMap.get(amount);
        if (!subMap.containsKey(operation)) {
            subMap.put(operation, new AttributeModifier("DangerZone", amount, operation));
        }
        return subMap.get(operation);
    }

    public void applyModifier(EntityLivingBase e, IAttribute attr, double amount, int operation) {
        IAttributeInstance inst = e.getAttributeMap().getAttributeInstance(attr);
        AttributeModifier modifier = get(amount, operation);

        if (!inst.hasModifier(modifier)) {
            inst.applyModifier(modifier);
        }
    }
}
