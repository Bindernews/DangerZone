package com.vortexel.dangerzone.client.gui;

import lombok.val;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class BaseGuiContainer extends GuiContainer {

    protected ResourceLocation backgroundTexture;

    public BaseGuiContainer(Container container, ResourceLocation backgroundTexture) {
        super(container);
        this.backgroundTexture = backgroundTexture;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(backgroundTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize - 1, ySize - 1);
    }
}
