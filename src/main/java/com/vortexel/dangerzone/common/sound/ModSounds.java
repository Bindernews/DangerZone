package com.vortexel.dangerzone.common.sound;

import com.vortexel.dangerzone.DangerZone;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModSounds {

    public static final SoundEvent shotgunFire = makeSound("shotgun_fire");

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new Registrar());
    }

    public static class Registrar {
        @SubscribeEvent
        public void register(RegistryEvent.Register<SoundEvent> event) {
            IForgeRegistry<SoundEvent> r = event.getRegistry();
            r.register(shotgunFire);
        }
    }

    public static SoundEvent makeSound(String resourceName) {
        ResourceLocation loc = DangerZone.prefix(resourceName);
        return new SoundEvent(loc).setRegistryName(loc);
    }

    private ModSounds() {}
}
