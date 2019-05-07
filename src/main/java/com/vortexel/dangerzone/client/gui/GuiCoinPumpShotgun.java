package com.vortexel.dangerzone.client.gui;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.gui.ContainerCoinPouch;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiCoinPumpShotgun extends BaseGuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = DangerZone.prefix("textures/gui/coin_pump_shotgun.png");

    public GuiCoinPumpShotgun(Container inventorySlotsIn) {
        super(inventorySlotsIn, BACKGROUND_TEXTURE);
        xSize = 176;
        ySize = 166;
    }
/*
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        //val amount = ItemCoinPouch.getAmount(getContainer().getCoinPumpShotgun());
        val coinText = I18n.format("gui.dangerzone.coins", amount);
        fontRenderer.drawString(coinText, 12, 64, Consts.COLOR_BLACK);
    }
*/
}
