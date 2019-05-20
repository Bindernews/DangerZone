package com.vortexel.dangerzone.common.item;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.vortexel.dangerzone.common.entity.EntityCoinProjectile;
import com.vortexel.dangerzone.common.gui.GuiHandler;
import com.vortexel.dangerzone.common.sound.ModSounds;
import com.vortexel.dangerzone.common.util.FnUtil;
import com.vortexel.dangerzone.common.util.Hitscan;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class ItemCoinPumpShotgun extends BaseItem {

    private static final String KEY_CONTENTS = "contents";
    private static final double SHOT_DISTANCE = 200;
    private static final float RAY_RADIUS = 1f;
    private static float INACCURACY = 10f;

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
                world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.shotgunDryFire,
                        SoundCategory.PLAYERS, 0.75f, 1f);
                player.getCooldownTracker().setCooldown(this, 10);
                return;
            } else { //Go ahead and fire the shotgun
                world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.shotgunFire,
                        SoundCategory.PLAYERS, 1f, 1f);
                val ammoType = ((ItemLootCoin) ammo.getItem());
                ammo.grow(-1);
                setContents(shotgun, ammo);
                //Actual firing of weapon
                fireShot(player, player.getLookVec(), INACCURACY, ammoType);
                // Set cooldown timer
                player.getCooldownTracker().setCooldown(this, 25);
            }
        }
    }

    public void fireShot(EntityLivingBase entity, Vec3d towards, float inaccuracy, ItemLootCoin coinType) {
        val world = entity.getEntityWorld();
        val start = entity.getPositionVector().addVector(0, entity.getEyeHeight() - 0.1, 0);
        float damage = 0f;
        int bullets = 0;
        switch (coinType.amount){
            case 1:
                damage = 2;
                bullets = 1;
                break;
            case 8:
                damage = 4;
                bullets = 4;
                break;
            case 64:
                damage = 16;
                bullets = 4;
                break;
            case 512:
                damage = 128;
                bullets = 6;
        }
        // Create the filter for the hitscan to use so we only get the entities we want.
        val hitscanFilter = makeEntityFilterPredicate();

        // List of entities that we hit.
        List<EntityLivingBase> hitEntities = Lists.newArrayListWithCapacity(bullets);
        for (int i = 0; i < bullets; i++) {
            val motion = towards.add(inaccuracyVec(world.rand, inaccuracy));
            val end = start.add(motion.scale(SHOT_DISTANCE));
            val rayTrace = world.rayTraceBlocks(start, end, false, true, false);
            val scan = new Hitscan(entity.getEntityWorld(), start, end, rayTrace, RAY_RADIUS, hitscanFilter);
            val entityHit = scan.findFirstNot(entity);
            if (entityHit != null) {
                // If we hit something, put it in the list of things we hit
                hitEntities.add((EntityLivingBase)entityHit);
            } else {
                //val endPos = scan.getEnd();
                //val marker = new EntityTippedArrow(world, endPos.x, endPos.y, endPos.z);
                //world.spawnEntity(marker);
            }
        }
        // Spawn the bullets. If an entity is hit multiple times, then they take more damage.
        for (int i = 0; i < hitEntities.size(); i++) {
            if (hitEntities.get(i) != null) {
                int bulletCount = 1;
                // Loop through hitEntities and find all other instances of this entity.
                // For each, increment the number of bullets and then make sure we don't process it twice.
                for (int j = i + 1; j < hitEntities.size(); j++) {
                    if (hitEntities.get(i) == hitEntities.get(j)) {
                        bulletCount++;
                        hitEntities.set(j, null);
                    }
                }
                spawnCoinBulletAt(entity, hitEntities.get(i), coinType, damage * bulletCount);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Predicate<Entity> makeEntityFilterPredicate() {
        return Predicates.and(EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE,
                (e) -> e instanceof EntityLivingBase);
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
