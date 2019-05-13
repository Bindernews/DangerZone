package com.vortexel.dangerzone.common.sound;

import com.vortexel.dangerzone.DangerZone;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModSounds {

    public static final SoundEvent shotgunFire = makeSound("shotgun_fire");
    public static final SoundEvent shotgunDryFire = makeSound("shotgun_dry_fire");

    public static final SoundEvent[] SOUNDS = new SoundEvent[] {
            shotgunFire,
            shotgunDryFire,
    };

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ModSounds.class);
    }

    protected static SoundEvent makeSound(String resourceName) {
        ResourceLocation loc = DangerZone.prefix(resourceName);
        return new SoundEvent(loc).setRegistryName(loc);
    }

    @SubscribeEvent
    public static void register(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(SOUNDS);
    }

    private ModSounds() {}
}
