package com.vortexel.dangerzone.common.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;

public interface IVillager {

    EntityCreature getEntity();

    boolean isTrading();

    EntityPlayer getCustomer();

    void setCustomer(EntityPlayer customer);
}
