package com.vortexel.dangerzone.common;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.capability.IDangerLevel;
import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * Minecraft utility methods.
 */
public class MCUtil {

    private static final char SEP_CHAR = '.';

    /**
     * Takes any Event that deals with an Entity and returns true if the world is local.
     */
    public static boolean isWorldLocal(EntityEvent e) {
        return isWorldLocal(e.getEntity());
    }

    public static boolean isWorldLocal(Entity e) {
        return !e.getEntityWorld().isRemote;
    }

    /**
     * Return true if the world containing {@code entity} is enabled in the config.
     */
    public static boolean isWorldEnabled(Entity entity) {
        return isWorldEnabled(entity.getEntityWorld());
    }

    /**
     * Return true if {@code world} has danger zone enabled for it in the config.
     */
    public static boolean isWorldEnabled(World world) {
        return DZConfig.INSTANCE.getWorld(world.provider.getDimension()).enabled;
    }

    public static String translationKey(String prefix, String... suffixes) {
        StringBuilder sb = new StringBuilder(prefix.length() + DangerZone.ID.length() + 2);
        sb.append(prefix);
        sb.append(SEP_CHAR);
        sb.append(DangerZone.ID);
        sb.append(':');
        for (String suffix : suffixes) {
            sb.append(suffix);
            sb.append(SEP_CHAR);
        }
        if (sb.charAt(sb.length() - 1) == SEP_CHAR) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static IDangerLevel getDangerLevelCapability(Entity e) {
        return e.getCapability(DangerZone.CAP_DANGER_LEVEL, null);
    }

    public static EntityItem makeItemAt(Entity other, ItemStack stack) {
        val pos = other.getPosition();
        return new EntityItem(other.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    private MCUtil() {}
}
