package com.vortexel.dangerzone.common.entity;

import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityCoinProjectile extends EntityThrowable {

    private ItemLootCoin coinType;

    public EntityCoinProjectile(World world, EntityLivingBase attacker, ItemLootCoin coinType) {
        super(world, attacker);
        this.coinType = coinType;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (ticksExisted >= Consts.TICKS_PER_SECOND && MCUtil.isWorldLocal(this))
        {
            this.setDead();
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (MCUtil.isWorldLocal(world))
        {
            val arrow = new EntityTippedArrow(world, this.posX, this.posY, this.posZ);
            world.spawnEntity(arrow);

            this.setDead();
        }
    }
}
