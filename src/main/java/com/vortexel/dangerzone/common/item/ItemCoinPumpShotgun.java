package com.vortexel.dangerzone.common.item;

import com.google.common.base.Predicates;
import com.vortexel.dangerzone.common.entity.EntityCoinProjectile;
import com.vortexel.dangerzone.common.gui.GuiHandler;
import com.vortexel.dangerzone.common.sound.ModSounds;
import com.vortexel.dangerzone.common.util.FnUtil;
import com.vortexel.dangerzone.common.util.Hitscan;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class ItemCoinPumpShotgun extends BaseItem {

    private static final String KEY_CONTENTS = "contents";
    private static final double SHOT_DISTANCE = 20;
    private static final float RAY_RADIUS = 0.5f;

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
                player.playSound(ModSounds.shotgunFire, 0.75f, 1);
//                world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.shotgunFire,
//                        SoundCategory.BLOCKS, 0.2F,
//                        ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                return;
            } else { //Go ahead and fire the shotgun
                val ammoType = ((ItemLootCoin) ammo.getItem());
                ammo.grow(-1);
                setContents(shotgun, ammo);
                //Actual firing of weapon
                fireShot(player, player.getLookVec(), 0.5f, ammoType, 4);
                // Set cooldown timer
                player.getCooldownTracker().setCooldown(this, 40);
            }
        }
    }

    public void fireShot(EntityLivingBase entity, Vec3d towards, float inaccuracy, ItemLootCoin coinType, int bullets) {
        val world = entity.getEntityWorld();
        val start = entity.getPositionVector();
        // Calculate the actual damage
        float realDamage = (float)coinType.amount / bullets;
        if (realDamage < 1f) {
            realDamage = 1f;
        }
        // Create the filter for the hitscan to use so we only get the entities we want.
        val hitscanFilter = Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE,
                (e) -> e instanceof EntityLivingBase);
        for (int i = 0; i < bullets; i++) {
            val motion = towards.add(inaccuracyVec(world.rand, inaccuracy));
            val end = start.add(motion.scale(SHOT_DISTANCE));
            val scan = new Hitscan(entity.getEntityWorld(), start, end, RAY_RADIUS, hitscanFilter);
            val entityHit = scan.findFirstNot(entity);
            if (entityHit != null) {
                spawnCoinBulletAt(entity, (EntityLivingBase)entityHit, coinType, realDamage);
            }
        }
    }

    private void spawnCoinBulletAt(EntityLivingBase attacker, EntityLivingBase victim, ItemLootCoin coinType,
                                   float damage) {
        val world = victim.getEntityWorld();
        val coin = new EntityCoinProjectile(world, attacker, coinType);
        coin.setPosition(victim.posX, victim.posY, victim.posZ);
        world.spawnEntity(coin);
        victim.attackEntityFrom(DamageSource.causeThrownDamage(coin, attacker), damage);
    }

    private static Vec3d inaccuracyVec(Random rand, float inaccuracy) {
        val x = rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        val y = rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        val z = rand.nextGaussian() * 0.007499999832361937D * (double)inaccuracy;
        return new Vec3d(x, y, z);
    }
}
