package com.vortexel.dangerzone.common.entity;

import com.vortexel.dangerzone.common.entity.ai.AITaskTradePlayer;
import com.vortexel.dangerzone.common.entity.ai.IVillager;
import com.vortexel.dangerzone.common.gui.ContainerTradeVillager;
import com.vortexel.dangerzone.common.gui.GuiHandler;
import com.vortexel.dangerzone.common.util.MCUtil;
import lombok.Getter;
import lombok.val;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityTraderVillager extends EntityVillager implements IVillager {

    @Getter
    protected EntityPlayer customer;

    public EntityTraderVillager(World worldIn) {
        super(worldIn);
    }

    @SuppressWarnings("unchecked")
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
        this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityEvoker.class, 12.0F, 0.8D, 0.8D));
        this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityVindicator.class, 8.0F, 0.8D, 0.8D));
        this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityVex.class, 8.0F, 0.6D, 0.6D));
        this.tasks.addTask(1, new AITaskTradePlayer(this));
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWanderAvoidWater(this, 0.6D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        val inHand = player.getHeldItem(hand);
        val isNameTag = inHand.getItem() == Items.NAME_TAG;

        if (isNameTag) {
            inHand.interactWithEntity(player, this, hand);
            return true;
        } else if (isEntityAlive() && !isTrading() && !player.isSneaking() && MCUtil.isWorldLocal(world)) {
            startTrading(player);
            return true;
        } else {
            return super.processInteract(player, hand);
        }
    }

    public void startTrading(EntityPlayer player) {
        setCustomer(player);
        GuiHandler.openGui(player, GuiHandler.GUI_TRADER, getPosition());
    }

    @Override
    public EntityCreature getEntity() {
        return this;
    }

    @Override
    public boolean isTrading() {
        return getCustomer() != null;
    }

    @Override
    public void setCustomer(@Nullable EntityPlayer player) {
        // Make sure the current customer is no longer interacting if we're trading with someone else.
        if (customer != null && customer.openContainer instanceof ContainerTradeVillager) {
            customer.closeScreen();
        }
        customer = player;
    }
}
