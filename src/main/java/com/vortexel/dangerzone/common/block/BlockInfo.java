package com.vortexel.dangerzone.common.block;

import lombok.Builder;
import lombok.Value;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.function.BiFunction;

@Builder
@Value
public class BlockInfo {

    public Material material;
    public String name;
    public float hardness;
    public float lightLevel;
    public IProperty<?> property;
    public Comparable<?> defaultState;
    public BiFunction<World, IBlockState, TileEntity> tileEntity;

    public static BlockInfoBuilder builder(String name) {
        return new BlockInfoBuilder().name(name);
    }
}
