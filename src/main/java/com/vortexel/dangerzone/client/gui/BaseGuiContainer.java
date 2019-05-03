package com.vortexel.dangerzone.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public class BaseGuiContainer extends GuiContainer {

    protected ResourceLocation backgroundTexture;

    /**
     * @param container the container to reference
     * @param backgroundTexture a 256x256 texture. If the dimensions are wrong, it will look weird.
     */
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

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }
}
