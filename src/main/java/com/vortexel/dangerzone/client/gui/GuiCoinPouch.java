package com.vortexel.dangerzone.client.gui;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.gui.ContainerCoinPouch;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import com.vortexel.dangerzone.common.item.ModItems;
import lombok.val;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiCoinPouch extends BaseGuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = DangerZone.prefix("textures/gui/coin_pouch.png");

    private static final ItemStack STACK_COIN_1 = new ItemStack(ModItems.lootCoin_1);
    private static final ItemStack STACK_COIN_8 = new ItemStack(ModItems.lootCoin_8);
    private static final ItemStack STACK_COIN_64 = new ItemStack(ModItems.lootCoin_64);
    private static final ItemStack STACK_COIN_512 = new ItemStack(ModItems.lootCoin_512);

    public GuiCoinPouch(Container inventorySlotsIn) {
        super(inventorySlotsIn, BACKGROUND_TEXTURE);
        xSize = 176;
        ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
    }

    @Override
    public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        renderItem(STACK_COIN_1, 70, 28);
        renderItem(STACK_COIN_8, 88, 28);
        renderItem(STACK_COIN_64, 106, 28);
        renderItem(STACK_COIN_512, 124, 28);
    }

    protected void renderItem(ItemStack stack, int x, int y) {
        itemRender.renderItemIntoGUI(stack, x + guiLeft, y + guiTop);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        val amount = ItemCoinPouch.getAmount(getContainer().getCoinPouch());
        val coinText = I18n.format("gui.dangerzone.coins", amount);
        fontRenderer.drawString(coinText, 12, 64, Consts.COLOR_BLACK);
    }

    public ContainerCoinPouch getContainer() {
        return (ContainerCoinPouch)this.inventorySlots;
    }
}
