package com.vortexel.dangerzone.client.gui;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.client.gui.component.BScrollBar;
import com.vortexel.dangerzone.client.gui.component.IGuiComponent;
import com.vortexel.dangerzone.common.gui.ContainerTradeVillager;
import com.vortexel.dangerzone.common.network.PacketContainerUpdate;
import com.vortexel.dangerzone.common.trade.MerchandiseManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GuiTradeVillager extends BaseGuiContainer {

    private static final ResourceLocation BG_TEXTURE = DangerZone.prefix("textures/gui/trade_gui.png");

    protected GuiContainerHelper helper;
    protected BScrollBar scrollBar;

    public GuiTradeVillager(Container container) {
        super(container, BG_TEXTURE);
        helper = new GuiContainerHelper(this);
        xSize = 194;
        ySize = 186;
    }

    @Override
    public void initGui() {
        super.initGui();
        // Build the scroll bar
        int maxRows = (int)Math.ceil((float)getMerchandise().getTotalOffers() / ContainerTradeVillager.COLS);
        int maxScroll = Math.max(maxRows - ContainerTradeVillager.ROWS, 0);
        scrollBar = new BScrollBar(this, 174, 18, 12, 70,
                new Sprite(BG_TEXTURE, 232, 0, 12, 15, 256, 256));
        scrollBar.setRange(0, maxScroll, 1);
        scrollBar.scrollListeners.add(this::onScroll);
        components.add(scrollBar);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        helper.beginDraw(mouseX, mouseY, partialTicks);
        // Draw background
        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        // Draw buttons and labels
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        helper.drawButtonsAndLabels(getButtonList(), getLabelList());
        // Make it so (0,0) is the top-left of the GUI
        GlStateManager.pushMatrix();
        helper.translateTopLeft();
        // Draw inventory slots
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        helper.drawSlots(inventorySlots.inventorySlots);
        // Draw foreground
        RenderHelper.disableStandardItemLighting();
        drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        // Draw currently held stack and the stack being returned (if there is one)
        helper.drawHeldStack();
        helper.drawReturningStack();
        // Reset GL state
        helper.resetGLState();
        // Render the components
        for (IGuiComponent comp : components) {
            comp.draw();
        }
        GlStateManager.popMatrix();
        // Render the tooltip
        renderHoveredToolTip(mouseX, mouseY);
    }

    protected void onScroll(float row) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("row", (int)row);
        getContainer().sendUpdatePacket(mc.player, tag);
    }

    public ContainerTradeVillager getContainer() {
        return (ContainerTradeVillager)inventorySlots;
    }

    public static MerchandiseManager getMerchandise() {
        return DangerZone.proxy.getMerchandise();
    }
}
