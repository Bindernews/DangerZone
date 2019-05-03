package com.vortexel.dangerzone.common.item;

import com.vortexel.dangerzone.common.DangerMath;
import com.vortexel.dangerzone.common.gui.GuiHandler;
import com.vortexel.dangerzone.common.util.FnUtil;
import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemCoinPouch extends BaseItem {

    private static final String AMOUNT_KEY = "amount";

    public ItemCoinPouch() {
        super("coin_pouch");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        GuiHandler.openGui(playerIn, GuiHandler.GUI_COIN_POUCH);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        val tKey = getUnlocalizedName() + ".tooltip";
        val amount = Long.toString(getAmount(stack));
        tooltip.add(I18n.format(tKey, amount));
    }

    public static long getAmount(@Nonnull ItemStack stack) {
        assertIsCoinPouch(stack);
        val tag = stack.getTagCompound();
        if (tag == null) {
            return 0;
        } else {
            return tag.getLong(AMOUNT_KEY);
        }
    }

    public static void setAmount(@Nonnull ItemStack stack, long amount) {
        assertIsCoinPouch(stack);
        Validate.inclusiveBetween(0, Integer.MAX_VALUE, amount);
        val tag = FnUtil.orElse(stack.getTagCompound(), new NBTTagCompound());
        tag.setLong(AMOUNT_KEY, amount);
        stack.setTagCompound(tag);
    }

    public static void addAmount(@Nonnull ItemStack stack, long amount) {
        assertIsCoinPouch(stack);
        val tag = FnUtil.orElse(stack.getTagCompound(), new NBTTagCompound());
        val current = tag.getLong(AMOUNT_KEY);
        val next = DangerMath.clamp(current + amount, 0, Long.MAX_VALUE);
        tag.setLong(AMOUNT_KEY, next);
        stack.setTagCompound(tag);
    }

    private static void assertIsCoinPouch(@Nonnull ItemStack stack) {
        if (stack.getItem() != ModItems.coinPouch) {
            throw new UnsupportedOperationException(String.format("Expected %1$s got %2$s",
                    ModItems.coinPouch.getRegistryName(), stack.getItem().getRegistryName()));
        }
    }
}
