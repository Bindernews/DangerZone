package com.vortexel.dangerzone.common.api;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.api.IDangerZoneAPI;
import com.vortexel.dangerzone.api.trading.IMerchandiseRegistry;
import com.vortexel.dangerzone.common.DangerMath;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.World;

import java.io.Reader;

public class ImplDangerZoneAPI implements IDangerZoneAPI {

    @Getter @Setter
    private boolean frozen;

    @Override
    public double getDangerLevel(World world, int blockX, int blockZ) {
        return DangerMath.dangerLevel(DangerZone.proxy.getDifficulty(world, blockX, blockZ));
    }

    @Override
    public void addEntityConfig(Reader source) {
        if (!isFrozen()) {
            DangerZone.proxy.getEntityConfigManager().addFile(source);
        }
    }

    @Override
    public void addEntityConfig(String source) {
        if (!isFrozen()) {
            DangerZone.proxy.getEntityConfigManager().addFile(source);
        }
    }

    @Override
    public IMerchandiseRegistry getMerchandise() {
        return DangerZone.proxy.getMerchandise();
    }
}
