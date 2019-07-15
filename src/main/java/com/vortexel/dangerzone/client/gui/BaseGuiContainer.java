package com.vortexel.dangerzone.client.gui;

import com.google.common.collect.Lists;
import com.vortexel.dangerzone.client.gui.component.IGuiComponent;
import com.vortexel.dangerzone.common.util.Reflector;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BaseGuiContainer extends GuiContainer {


    private static final Method mDrawSlot = Reflector.findMethodDeobf(GuiContainer.class,
            "drawSlot", "func_146977_a", Void.class, Slot.class);
    private static final Field fDraggedStack = getF("field_147012_x", "draggedStack"); // "y"
    private static final Field fIsMouseRightClick = getF("field_147004_w", "isMouseRightClick"); // "x"
    private static final Field fHoveredSlot = getF("field_147006_u", "hoveredSlot"); // "v"
    private static final Field fTouchUpX = getF("field_147011_y", "touchUpX"); // "z"
    private static final Field fTouchUpY = getF("field_147010_z", "touchUpY"); // "A"
    private static final Field fReturningStack = getF("field_146991_C", "returningStack");
    private static final Field fReturningStackDestSlot = getF("field_146989_A", "returningStackDestSlot");
    private static final Field fDragSplittingRemnant = getF("field_146996_I", "dragSplittingRemnant");
    private static final Field fReturningStackTime = getF("field_146990_B", "returningStackTime");

    protected ResourceLocation backgroundTexture;
    protected List<IGuiComponent> components;

    /**
     * @param container the container to reference
     * @param backgroundTexture a 256x256 texture. If the dimensions are wrong, it will look weird.
     */
    public BaseGuiContainer(Container container, ResourceLocation backgroundTexture) {
        super(container);
        this.backgroundTexture = backgroundTexture;
        this.components = Lists.newArrayList();
    }

    @Override
    public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(backgroundTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize - 1, ySize - 1);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        for (IGuiComponent comp : components) {
            comp.draw();
        }
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        for (IGuiComponent comp : components) {
            // Make mouseX and mouseY relative to the GUI
            comp.onMouseClick(mouseX - guiLeft, mouseY - guiTop, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        for (IGuiComponent comp : components) {
            // These mouseX and mouseY are already relative to the GUI
            comp.onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for (IGuiComponent comp : components) {
            // Make mouseX and mouseY relative to the GUI
            comp.onMouseRelease(mouseX - guiLeft, mouseY - guiTop, state);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        for (IGuiComponent comp : components) {
            comp.handleMouseInput();
        }
    }

    // region Accessors

    @Override
    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        super.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    @Override
    public void renderHoveredToolTip(int mouseX, int mouseY) {
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    public void drawSlotHelper(Slot slot) {
        Reflector.callMethod(this, mDrawSlot, slot);
    }

    public ItemStack getDraggedStack() {
        return Reflector.get(this, fDraggedStack);
    }

    public void setDraggedStack(ItemStack stack) {
        Reflector.set(this, fDraggedStack, stack);
    }

    public boolean isMouseRightClick() {
        return Reflector.get(this, fIsMouseRightClick);
    }

    public Slot getHoveredSlot() {
        return Reflector.get(this, fHoveredSlot);
    }

    public void setHoveredSlot(Slot s) {
        Reflector.set(this, fHoveredSlot, s);
    }

    public ItemStack getReturningStack() {
        return Reflector.get(this, fReturningStack);
    }

    public void setReturningStack(ItemStack s) {
        Reflector.set(this, fReturningStack, s);
    }

    public int getTouchUpX() {
        return Reflector.get(this, fTouchUpX);
    }

    public int getTouchUpY() {
        return Reflector.get(this, fTouchUpY);
    }

    public Slot getReturningStackDestSlot() {
        return Reflector.get(this, fReturningStackDestSlot);
    }

    public RenderItem getItemRender() {
        return this.itemRender;
    }

    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    public List<GuiButton> getButtonList() {
        return buttonList;
    }

    public List<GuiLabel> getLabelList() {
        return labelList;
    }

    public int getDragSplittingRemnant() {
        return Reflector.get(this, fDragSplittingRemnant);
    }

    public boolean isDragSplitting() {
        return dragSplitting;
    }

    public void setDragSplitting(boolean v) {
        dragSplitting = v;
    }

    public Set<Slot> getDragSplittingSlots() {
        return dragSplittingSlots;
    }

    public long getReturningStackTime() {
        return Reflector.get(this, fReturningStackTime);
    }

    protected static Field getF(String srgName, String name) {
        return Reflector.findFieldDeobf(GuiContainer.class, name, srgName);
    }

    // endregion Accessors
}
