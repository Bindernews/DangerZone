package com.vortexel.dangerzone.common.item;

import com.vortexel.dangerzone.common.entity.EntityCoinProjectile;
import com.vortexel.dangerzone.common.gui.GuiHandler;
import com.vortexel.dangerzone.common.util.FnUtil;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;

public class ItemCoinPumpShotgun extends BaseItem {

    private static final String KEY_CONTENTS = "contents";

    public ItemCoinPumpShotgun() {
        super("coin_pump_shotgun");
        setMaxStackSize(1);
    }

    public static void setContents(@Nonnull ItemStack stack, ItemStack contents) {
        val tag = FnUtil.orElse(stack.getTagCompound(), new NBTTagCompound());
        tag.setTag(KEY_CONTENTS, contents.serializeNBT());
        stack.setTagCompound(tag);
    }

    public static ItemStack getContents(@Nonnull ItemStack stack) {
        val tag = stack.getTagCompound();
        if (tag != null) {
            return new ItemStack(tag.getCompoundTag(KEY_CONTENTS));
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (playerIn.isSneaking()) {
            GuiHandler.openGui(playerIn, GuiHandler.GUI_COIN_PUMP_SHOTGUN);
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        } else
        {
            shotgunFire(playerIn, handIn);
            return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
    }

    private void shotgunFire(EntityPlayer player, EnumHand hand) {
        if (MCUtil.isWorldLocal(player)) {
            val shotgun = player.getHeldItem(hand);
            val ammo = getContents(shotgun);
            val world = player.getEntityWorld();
            if (ammo.isEmpty()) {
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                return;
            } else { //Go ahead and fire the shotgun
                val ammoType = ((ItemLootCoin) ammo.getItem());
                ammo.grow(-1);
                setContents(shotgun, ammo);
                //Actual firing of weapon
                for (int i = 0; i < 8; i++) {
                    val coin = new EntityCoinProjectile(world, player, ammoType);
                    coin.shoot(player, player.rotationPitch, player.rotationYaw, 0, 5F, 2);
                    world.spawnEntity(coin);
                    player.getPositionVector();
                }
                player.getCooldownTracker().setCooldown(this, 40);
            }
        }
    }

    //public void fireWeapon(World world, Vec3d location, float pitch, float yaw, int bullets, float inaccuracy, float damage)
}
