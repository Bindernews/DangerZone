package com.vortexel.dangerzone.common.entity.ai;

import com.vortexel.dangerzone.common.entity.EntityTraderVillager;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public class AITaskTradePlayer extends EntityAIBase {

    private final EntityTraderVillager villager;

    public AITaskTradePlayer(EntityTraderVillager villagerIn)
    {
        this.villager = villagerIn;
        this.setMutexBits(5);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityCreature entity = villager;
        if (!entity.isEntityAlive() || entity.isInWater() || !entity.onGround || entity.velocityChanged) {
            return false;
        } else {
            EntityPlayer player = this.villager.getCustomer();
            if (player == null) {
                return false;
            } else if (entity.getDistanceSq(player) > 16.0D) {
                return false;
            } else {
                return player.openContainer != null;
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        villager.getNavigator().clearPath();
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
        villager.setCustomer(null);
    }
}
