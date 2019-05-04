package com.vortexel.dangerzone.client.gui;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.gui.ContainerCoinPouch;
import com.vortexel.dangerzone.common.item.ItemCoinPouch;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import com.vortexel.dangerzone.common.network.PacketCoinPouchCoinType;
import com.vortexel.dangerzone.common.network.PacketHandler;
import lombok.val;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;

public class GuiCoinPouch extends BaseGuiContainer {

    private static final ResourceLocation BACKGROUND_TEXTURE = DangerZone.prefix("textures/gui/coin_pouch.png");

    public GuiCoinPouch(Container inventorySlotsIn) {
        super(inventorySlotsIn, BACKGROUND_TEXTURE);
        xSize = 176;
        ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        addButton(makeImageButton(0, 160, 32, 9, 9, 178, 3, 1));
        addButton(makeImageButton(1, 160, 42, 9, 9, 178, 12, 1));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            int i = ArrayUtils.indexOf(ItemLootCoin.AMOUNTS, getContainer().getOutputType().amount);
            // If we didn't find the value, or we're at the top, then do nothing
            if (i == ArrayUtils.INDEX_NOT_FOUND || i == ItemLootCoin.AMOUNTS.length - 1) {
                return;
            }
            requestNewCoin(i + 1);
        } else if (button.id == 1) {
            int i = ArrayUtils.indexOf(ItemLootCoin.AMOUNTS, getContainer().getOutputType().amount);
            // If we didn't find the value, or we're at the bottom, then do nothing
            if (i == ArrayUtils.INDEX_NOT_FOUND || i == 0) {
                return;
            }
            requestNewCoin(i - 1);
        }
    }

    private void requestNewCoin(int index) {
        ItemLootCoin nextCoin = ItemLootCoin.fromAmount(ItemLootCoin.AMOUNTS[index]);
        PacketHandler.NETWORK.sendToServer(new PacketCoinPouchCoinType(nextCoin, true));
    }

    private GuiButtonImage makeImageButton(int id, int x, int y, int width, int height, int texX, int texY,
                                                  int hoverYOffset) {
        return new GuiButtonImage(id, guiLeft + x, guiTop + y, width, height, texX, texY,
                hoverYOffset, BACKGROUND_TEXTURE);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        val amount = ItemCoinPouch.getAmount(getContainer().getCoinPouch());
        val coinValueStr = Integer.toString(getContainer().getOutputType().amount);
        val coinValueX = 149 - (fontRenderer.getStringWidth(coinValueStr) / 2);
        fontRenderer.drawString(I18n.format("gui.dangerzone.coins"), 66, 22, Consts.COLOR_BLACK);
        fontRenderer.drawString(Long.toString(amount), 66, 38, Consts.COLOR_BLACK);
        fontRenderer.drawString(coinValueStr, coinValueX, 22, Consts.COLOR_BLACK);
    }

    public ContainerCoinPouch getContainer() {
        return (ContainerCoinPouch)this.inventorySlots;
    }
}
