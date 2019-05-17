package com.vortexel.dangerzone.common.api;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.api.IDangerZoneAPI;
import com.vortexel.dangerzone.common.DangerMath;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.util.MCUtil;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ImplDangerZoneAPI implements IDangerZoneAPI {

    @Override
    public double getDangerLevel(World world, int blockX, int blockZ) {
        return DangerMath.dangerLevel(DangerZone.proxy.getDifficulty(world, blockX, blockZ));
    }

    @Override
    public int getEntityLevel(Entity entity) {
        IDangerLevel level = MCUtil.getDangerLevelCapability(entity);
        if (level != null) {
            return level.getDanger();
        } else {
            return 0;
        }
    }
}
