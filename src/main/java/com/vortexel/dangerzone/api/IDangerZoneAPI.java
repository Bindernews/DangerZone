package com.vortexel.dangerzone.api;

import com.vortexel.dangerzone.common.capability.IDangerLevel;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * The DangerZone API. Use this to query danger level of both areas and entities.
 * Get a reference to an implementation of this interface by calling:
 *     FMLInterModComms.sendFunctionMessage("dangerzone", "getAPI", "<your_package>.YourClass$GetTheAPI");
 */
public interface IDangerZoneAPI {
    /**
     * Get the danger level at a specific location in the given world.
     * @param world the world to query information on
     * @param blockX the X-portion of the location to query
     * @param blockZ the Z-portion of the location to query
     * @return the danger level at the given location or -1 if the danger level is unknown.
     */
    double getDangerLevel(World world, int blockX, int blockZ);

    /**
     * Get the IDangerLevel capability of {@code entity} or 0 if the {@link Entity} doesn't have a
     * an {@link IDangerLevel}.
     * @param entity the entity to get the danger level of
     * @return the entity's danger level or 0.
     */
    int getEntityLevel(Entity entity);
}
