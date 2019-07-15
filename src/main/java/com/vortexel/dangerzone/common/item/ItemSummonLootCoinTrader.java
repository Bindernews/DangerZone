package com.vortexel.dangerzone.common.item;

import com.vortexel.dangerzone.common.entity.EntityTraderVillager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSummonLootCoinTrader extends BaseItem {
    public ItemSummonLootCoinTrader() {
        super("summon_loot_coin_trader");
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack itemstack = player.getHeldItem(hand);

        if (worldIn.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        else if (!player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
            return EnumActionResult.FAIL;
        }
        else {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();

            BlockPos blockpos = pos.offset(facing);
            EntityTraderVillager entity = new EntityTraderVillager(worldIn);
            entity.setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
            worldIn.spawnEntity(entity);
            if (itemstack.hasDisplayName()) {
                entity.setCustomNameTag(itemstack.getDisplayName());
            }
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }
    }
}
