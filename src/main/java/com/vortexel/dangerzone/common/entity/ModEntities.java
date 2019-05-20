package com.vortexel.dangerzone.common.entity;

import com.vortexel.dangerzone.DangerZone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class ModEntities {

    public static final EntityEntry traderVillager = make(EntityTraderVillager.class, "trader_villager",
            1, 5651507, 12422002);

    public static final EntityEntry[] ENTITIES = new EntityEntry[] {
            traderVillager
    };

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ModEntities.class);
    }

    protected static EntityEntry make(Class<? extends Entity> cls, String name, int id,
                                      int primaryColor, int secondaryColor) {
        return EntityEntryBuilder.create()
                .name(name)
                .entity(cls)
                .id(DangerZone.prefix(name), id)
                .egg(primaryColor, secondaryColor)
                .tracker(80, 3, true)
                .build();
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().registerAll(ENTITIES);
    }
}
