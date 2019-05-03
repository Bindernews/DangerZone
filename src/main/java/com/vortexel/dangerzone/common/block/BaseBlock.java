package com.vortexel.dangerzone.common.block;

import com.vortexel.dangerzone.DangerZone;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Objects;

public class BaseBlock extends Block {

    private final BlockInfo info;

    public BaseBlock(BlockInfo info) {
        super(Objects.requireNonNull(info.material));
        this.info = info;
        setLightLevel(info.lightLevel);
        setHardness(info.hardness);
        setRegistryName(DangerZone.MOD_ID, Objects.requireNonNull(info.name));
        setUnlocalizedName(Objects.requireNonNull(getRegistryName()).toString());
        setCreativeTab(DangerZone.creativeTab);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return info.tileEntity != null;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return info.tileEntity.apply(world, state);
    }
}
