package com.vortexel.dangerzone.common.integration;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import mcjty.theoneprobe.api.*;
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
            if (!world.isRemote) {
                val cap = MCUtil.getDangerLevelCapability(entity);
                if (cap != null) {
                    probeInfo.text(translate("misc.dangerzone.level") + " " + cap.getDanger());
                }
            }
        }

        public static String translate(String key) {
            return IProbeInfo.STARTLOC + key + IProbeInfo.ENDLOC;
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
