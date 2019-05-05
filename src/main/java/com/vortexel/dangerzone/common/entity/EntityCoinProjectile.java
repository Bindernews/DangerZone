package com.vortexel.dangerzone.common.entity;

import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityCoinProjectile extends EntityThrowable {

    private ItemLootCoin coinType;

    public EntityCoinProjectile(World world, EntityPlayer player, ItemLootCoin coinType) {
        super(world, player);
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
            if(result.entityHit != null)
            {
                int amount = coinType.amount / 8;
                if (amount == 0)
                {
                    amount ++;
                }
                val damage = DamageSource.causeThrownDamage(this, this.getThrower());
                result.entityHit.attackEntityFrom(damage, amount);
            }
            this.setDead();
        }
    }
}
