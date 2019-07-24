package com.vortexel.dangerzone.common.entity;

import com.vortexel.dangerzone.common.Consts;
import com.vortexel.dangerzone.common.item.ItemLootCoin;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.val;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
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
        if (ticksExisted >= Consts.TICKS_PER_SECOND && MCUtil.isWorldLocal(this)) {
            this.setDead();
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            this.setDead();
        }
    }

    /**
     * Spawn a coin bullet entity at the location of {@code victim}, this immediately does damage to {@code victim}.
     * @param attacker the entity attacking the victim
     * @param victim the entity being attacked
     * @param coinType what type of coin to use for visuals (if there were any visuals)
     * @param damage how much damage to do to the victim
     */
    public static void spawnCoinBulletAt(EntityLivingBase attacker, EntityLivingBase victim, ItemLootCoin coinType,
                                          float damage) {
        val world = victim.getEntityWorld();
        val coin = new EntityCoinProjectile(world, attacker, coinType);
        coin.setPosition(victim.posX, victim.posY, victim.posZ);
        world.spawnEntity(coin);
        victim.attackEntityFrom(DamageSource.causeThrownDamage(coin, attacker), damage);
    }
}
