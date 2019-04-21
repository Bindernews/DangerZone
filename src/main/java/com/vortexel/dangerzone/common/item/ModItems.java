package com.vortexel.dangerzone.common.item;

import com.google.common.collect.Sets;
import com.vortexel.dangerzone.DangerZone;
import lombok.val;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.Set;

@GameRegistry.ObjectHolder(DangerZone.ID)
public class ModItems {

    // These will get filled in because of the ObjectHolder annotation

    public static final ItemLootBag loot_bag = nullz();

    @Mod.EventBusSubscriber(modid = DangerZone.ID)
    public static class Registrar {
        public static final Set<Item> ITEMS = Sets.newHashSet();

        /**
         * Register this mod's {@link Item}s
         */
        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> e) {
            final Item[] items = {
                    new ItemLootBag(),
            };

            val registry = e.getRegistry();
            for (val item : items) {
                registry.register(item);
                ITEMS.add(item);
            }
        }
    }

    public static Set<Item> getItems() {
        return Registrar.ITEMS;
    }


    @SuppressWarnings({"ConstantConditions", "SameReturnValue"})
    @Nonnull
    private static <T> T nullz() {
        return null;
    }
}
