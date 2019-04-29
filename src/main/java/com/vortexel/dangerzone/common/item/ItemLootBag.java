package com.vortexel.dangerzone.common.item;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.FnUtil;
import com.vortexel.dangerzone.common.MCUtil;
import com.vortexel.dangerzone.common.config.DZConfig;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.List;

public class ItemLootBag extends BaseItem {

    public static final String BAG_LEVEL_KEY = "bagLevel";

    public ItemLootBag() {
        super("loot_bag");
        setMaxStackSize(64);
    }

//    @Override
//    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
//                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
//        val parentResult = super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
//        if (parentResult != EnumActionResult.PASS) {
//            return parentResult;
//        }
//        return givePlayerLoot(player, worldIn, hand);
//    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        val parentResult = super.onItemRightClick(worldIn, playerIn, handIn);
        if (parentResult.getType() != EnumActionResult.PASS) {
            return parentResult;
        }
        return givePlayerLoot(playerIn, worldIn, handIn);
    }

    public ActionResult<ItemStack> givePlayerLoot(EntityPlayer player, World worldIn, EnumHand hand) {
        val stack = player.getHeldItem(hand);
        if (!(worldIn instanceof WorldServer) || stack.getItem() != this) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        val world = (WorldServer)worldIn;
        final int level = getLootBagLevel(stack);
        stack.grow(-1);
        if (level > 0) {
            val lootIter = DangerZone.proxy.getLootManager().getLootBagLoot(world, level).iterator();
            ItemStack unplacedStack = null;
            // Keep going until we run out of items or space in the inventory
            while (lootIter.hasNext() && unplacedStack == null) {
                val item = lootIter.next();
                if (!player.inventory.addItemStackToInventory(item)) {
                    unplacedStack = item;
                }
            }
            // If we ran out of inventory space, then drop it on the GROUND
            val pos = player.getPosition();
            if (unplacedStack != null) {
                MCUtil.spawnItem(world, pos, unplacedStack);
                while (lootIter.hasNext()) {
                    MCUtil.spawnItem(world, pos, lootIter.next());
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
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
            Validate.inclusiveBetween(0, DZConfig.C.REAL_MAX_DANGER_LEVEL, level);
            val tag = FnUtil.orElse(stack.getTagCompound(), new NBTTagCompound());
            tag.setInteger(BAG_LEVEL_KEY, level);
            stack.setTagCompound(tag);
        } else {
            throw new RuntimeException("stack doesn't contain ItemLootBag");
        }
    }
}
