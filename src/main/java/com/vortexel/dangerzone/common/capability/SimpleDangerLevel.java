package com.vortexel.dangerzone.common.capability;

import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.Data;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;

/**
 * The default implementation of IDangerLevel.
 * I know it seems dumb that I have IDangerLevel as an interface, but I just feel like it's the right call.
 * Maybe someone someday will want to add their own DangerLevel implementation or something.
 */
@Data
public class SimpleDangerLevel implements IDangerLevel {
    @Getter
    private int danger = 0;

    @Override
    public void setDanger(int v) {
        danger = MathHelper.clamp(v, 0, DZConfig.general.maxDangerLevel);
    }
}
