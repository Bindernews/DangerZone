package com.vortexel.dangerzone.client.gui;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.gui.ContainerCoinPouch;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class GuiCoinPouch extends BaseGuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = DangerZone.prefix("textures/gui/coin_pouch.png");

    public GuiCoinPouch(Container inventorySlotsIn) {
        super(inventorySlotsIn, BACKGROUND_TEXTURE);
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        val amount = ItemCoinPouch.getAmount(getContainer().getCoinPouch());
        fontRenderer.drawString(I18n.format("gui.dangerzone.coins"), 66, 22, Consts.COLOR_BLACK);
        fontRenderer.drawString(Long.toString(amount), 66, 38, Consts.COLOR_BLACK);
    }

    public ContainerCoinPouch getContainer() {
        return (ContainerCoinPouch)this.inventorySlots;
    }
}
