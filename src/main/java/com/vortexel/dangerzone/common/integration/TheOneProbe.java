package com.vortexel.dangerzone.common.integration;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.util.MCUtil;
import mcjty.theoneprobe.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.function.Function;

public class TheOneProbe {
    public static ITheOneProbe topApi;

    public static void init(ITheOneProbe api) {
        topApi = api;

        topApi.registerEntityProvider(new DangerLevelProvider());
    }

    public static class DangerLevelProvider implements IProbeInfoEntityProvider {
        @Override
        public String getID() {
            return DangerZone.MOD_ID + ":level";
        }

        @Override
        public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
                                       Entity entity, IProbeHitEntityData data) {
            IDangerLevel dangerLevel = MCUtil.getDangerLevelCapability(entity);
            if (dangerLevel != null) {
                probeInfo.text(I18n.format("misc.dangerzone.level", dangerLevel.getDanger()));
            }
        }
    }

    public static class GetAPI implements Function<ITheOneProbe, Void> {
        @Override
        public Void apply(ITheOneProbe api) {
            init(api);
            return null;
        }
    }
}
