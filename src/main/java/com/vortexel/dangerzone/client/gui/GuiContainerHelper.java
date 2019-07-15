package com.vortexel.dangerzone.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class GuiContainerHelper {

    public static final int SLOT_SIZE = 16;
    public static final int SLOT_GRADIENT_COLOR = -2130706433;

    public BaseGuiContainer gui;
    public int mouseX;
    public int mouseY;
    public float partialTicks;

    public GuiContainerHelper(BaseGuiContainer container) {
        this.gui = container;
    }


    public void beginDraw(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTicks = partialTicks;
        gui.setHoveredSlot(null);
    }


    public void drawSlots(List<Slot> inventorySlots) {
        drawSlots(inventorySlots, 0, inventorySlots.size());
    }

    public void drawSlots(List<Slot> inventorySlots, int start, int end) {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i1 = start; i1 < end; i1++)
        {
            Slot slot = inventorySlots.get(i1);

            if (slot.isEnabled()) {
                gui.drawSlotHelper(slot);
            }

            if (isMouseOverSlot(slot) && slot.isEnabled()) {
                gui.setHoveredSlot(slot);
                int j1 = slot.xPos;
                int k1 = slot.yPos;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.colorMask(true, true, true, false);
                gui.drawGradientRect(j1, k1, j1 + 16, k1 + 16, SLOT_GRADIENT_COLOR, SLOT_GRADIENT_COLOR);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }
    }

    public void drawHeldStack() {
        RenderHelper.disableStandardItemLighting();
        gui.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(gui, mouseX, mouseY));
        InventoryPlayer inventoryplayer = gui.mc.player.inventory;
        ItemStack draggedStack = gui.getDraggedStack();
        ItemStack heldStack = draggedStack.isEmpty() ? inventoryplayer.getItemStack() : draggedStack;

        if (!heldStack.isEmpty()) {
            int k2 = draggedStack.isEmpty() ? 8 : 16;
            String altText = null;

            if (!draggedStack.isEmpty() && gui.isMouseRightClick()) {
                heldStack = heldStack.copy();
                heldStack.setCount(MathHelper.ceil((float) heldStack.getCount() / 2.0F));
            } else if (gui.isDragSplitting() && gui.getDragSplittingSlots().size() > 1) {
                heldStack = heldStack.copy();
                heldStack.setCount(gui.getDragSplittingRemnant());

                if (heldStack.isEmpty()) {
                    altText = "" + TextFormatting.YELLOW + "0";
                }
            }

            drawItemStack(heldStack, mouseX - getLeft() - 8, mouseY - getTop() - k2, altText);
        }
    }

    public void drawReturningStack() {
        if (!gui.getReturningStack().isEmpty())
        {
            float f = (float)(Minecraft.getSystemTime() - gui.getReturningStackTime()) / 100.0F;

            if (f >= 1.0F)
            {
                f = 1.0F;
                gui.setReturningStack(ItemStack.EMPTY);
            }

            Slot returnStackDestSlot = gui.getReturningStackDestSlot();
            int l2 = returnStackDestSlot.xPos - gui.getTouchUpX();
            int i3 = returnStackDestSlot.yPos - gui.getTouchUpY();
            int l1 = gui.getTouchUpX() + (int)((float)l2 * f);
            int i2 = gui.getTouchUpY() + (int)((float)i3 * f);
            this.drawItemStack(gui.getReturningStack(), l1, i2, null);
        }
    }

    public void drawTheScreen() {
        // Draw background
        gui.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        // Draw buttons and labels
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        drawButtonsAndLabels(gui.getButtonList(), gui.getLabelList());
        // Make it so (0,0) is the top-left of the GUI
        translateTopLeft();
        // Draw slots
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawSlots(gui.inventorySlots.inventorySlots);
        // Draw foreground
        RenderHelper.disableStandardItemLighting();
        gui.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        // Draw currently held stack and the stack being returned (if there is one)
        drawHeldStack();
        drawReturningStack();
        // Reset GL state
        resetGLState();
        // Render the tooltip
        gui.renderHoveredToolTip(mouseX, mouseY);
    }

    public void translateTopLeft() {
        GlStateManager.translate(getLeft(), getTop(), 0.0F);
    }

    public void resetGLState() {
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.color(1, 1, 1, 1);
    }

    /**
     * Draws an ItemStack.
     *
     * The z index is increased by 32 (and not decreased afterwards), and the item is then rendered at z=200.
     */
    public void drawItemStack(ItemStack stack, int x, int y, String altText)
    {
        GlStateManager.translate(0.0F, 0.0F, 1.0F);
//        int yPos = y - (stack.isEmpty() ? 0 : 8);
        int yPos = y - (gui.getDraggedStack().isEmpty() ? 0 : 8);
        RenderItem ri = gui.getItemRender();
        ri.zLevel = 200.0F;
        ri.renderItemAndEffectIntoGUI(stack, x, y);
        ri.renderItemOverlayIntoGUI(getStackFontRenderer(stack), stack, x, yPos, altText);
        ri.zLevel = 0.0F;
    }

    public FontRenderer getStackFontRenderer(ItemStack stack) {
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        return font != null ? font : gui.getFontRenderer();
    }

    public boolean isMouseOverSlot(Slot slot) {
        return isMouseOverSlot(slot, mouseX, mouseY);
    }

    public boolean isMouseOverSlot(Slot slot, int mX, int mY) {
        return isPointInRegion(slot.xPos, slot.yPos, SLOT_SIZE, SLOT_SIZE, mX, mY);
    }


    public boolean isPointInRegion(int x, int y, int w, int h, int px, int py) {
        x += gui.getGuiLeft();
        y += gui.getGuiTop();
        return x <= px && y <= py && px <= (x + w) && py <= (y + h);
    }

    public void drawButtonsAndLabels(List<GuiButton> buttonList, List<GuiLabel> labelList) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        for (int i = 0; i < buttonList.size(); ++i) {
            ((GuiButton)buttonList.get(i)).drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
        }

        for (int j = 0; j < labelList.size(); ++j) {
            ((GuiLabel)labelList.get(j)).drawLabel(Minecraft.getMinecraft(), mouseX, mouseY);
        }
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
    }

    public int getLeft() {
        return gui.getGuiLeft();
    }

    public int getTop() {
        return gui.getGuiTop();
    }
}
