package com.vortexel.dangerzone.client.render;

import com.vortexel.dangerzone.DangerZone;
import com.vortexel.dangerzone.common.entity.EntityTraderVillager;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderTraderVillager extends RenderLiving<EntityTraderVillager> {

    public static final ResourceLocation TEXTURE = DangerZone.prefix("textures/entity/villager_trader.png");

    public RenderTraderVillager(RenderManager renderManager) {
        super(renderManager, new ModelVillager(0.0f), 0.5f);
        addLayer(new LayerCustomHead(((ModelVillager)getMainModel()).villagerHead));
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityTraderVillager entity) {
        return TEXTURE;
    }
}
