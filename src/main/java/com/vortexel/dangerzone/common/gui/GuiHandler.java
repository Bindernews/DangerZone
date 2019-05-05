package com.vortexel.dangerzone.common.gui;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.client.gui.GuiCoinPouch;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_COIN_POUCH = 1;
    public static final int GUI_TRADER = 3;

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        val container = getServerGuiElement(ID, player, world, x, y, z);
        if (container == null) {
            return null;
        }
        switch (ID) {
            case GUI_COIN_POUCH:
                return new GuiCoinPouch(container);
            case GUI_TRADER:

            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        switch (ID) {
            case GUI_COIN_POUCH:
                val stack = player.getHeldItem(EnumHand.MAIN_HAND);
                if (stack.getItem() == ModItems.coinPouch) {
                    return new ContainerCoinPouch(player);
                }
                return null;
            case GUI_TRADER:

            default:
                return null;
        }
    }

    public static void openGui(EntityPlayer player, int guiID) {
        val pos = player.getPosition();
        openGui(player, guiID, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void openGui(EntityPlayer player, int guiID, int x, int y, int z) {
        player.openGui(DangerZone.instance, guiID, player.getEntityWorld(), x, y, z);
    }

    public static void openGui(EntityPlayer player, int guiID, BlockPos location) {
        openGui(player, guiID, location.getX(), location.getY(), location.getZ());
    }
}
