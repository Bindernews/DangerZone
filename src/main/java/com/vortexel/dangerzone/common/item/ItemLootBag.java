package com.vortexel.dangerzone.common.item;

import com.vortexel.dangerzone.common.FnUtil;
import com.vortexel.dangerzone.common.MCUtil;
import com.vortexel.dangerzone.common.config.DZConfig;
import lombok.val;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLootBag extends DangerZoneItem {

    public static final String BAG_LEVEL_KEY = "bagLevel";

    public ItemLootBag() {
        super("loot_bag");
        setMaxStackSize(64);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        val tKey = getUnlocalizedName() + ".tooltip";
        val bagLevel = getLootBagLevel(stack);
        val bagLevelStr = bagLevel == -1 ? "ERROR" : Integer.toString(bagLevel);
        tooltip.add(I18n.format(tKey, bagLevelStr));
    }

    /**
     * Utility method to get the loot bag level of an ItemStack, or -1 if the ItemStack isn't an ItemLootBag
     * or it doesn't have a bag level key.
     */
    public static int getLootBagLevel(ItemStack stack) {
        if (stack.getItem() instanceof ItemLootBag) {
            val tag = stack.getTagCompound();
            if (tag == null || !tag.hasKey(BAG_LEVEL_KEY, Constants.NBT.TAG_INT)) {
                return -1;
            }
            return tag.getInteger(BAG_LEVEL_KEY);
        } else {
            return -1;
        }
    }

    /**
     * Set the bag level of {@code stack}. Throws an exception if {@code stack} isn't a stack of ItemLootBag.
     * @param stack the {@code ItemStack} to set the values for
     * @param level the loot bag level (range [0, 500])
     */
    public static void setLootBagLevel(ItemStack stack, int level) {
        if (stack.getItem() instanceof ItemLootBag) {
            Validate.inclusiveBetween(0, DZConfig.REAL_MAX_DANGER_LEVEL, level);
            val tag = FnUtil.orElse(stack.getTagCompound(), new NBTTagCompound());
            tag.setInteger(BAG_LEVEL_KEY, level);
            stack.setTagCompound(tag);
        } else {
            throw new RuntimeException("stack doesn't contain ItemLootBag");
        }
    }
}
