package com.vortexel.dangerzone.api;

import com.vortexel.dangerzone.api.trading.IMerchandiseRegistry;
import net.minecraft.world.World;

import java.io.Reader;

public interface IDangerZoneAPI {

    double getDangerLevel(World world, int blockX, int blockZ);

    void addEntityConfig(Reader source);

    void addEntityConfig(String source);

    IMerchandiseRegistry getMerchandise();
}
