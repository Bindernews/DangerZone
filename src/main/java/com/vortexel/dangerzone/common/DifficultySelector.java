package com.vortexel.dangerzone.common;

import net.minecraft.world.gen.NoiseGeneratorPerlin;

import java.util.Random;

public class DifficultySelector {

    private NoiseGeneratorPerlin generator;

    public DifficultySelector(long worldSeed) {
        generator = new NoiseGeneratorPerlin(new Random(worldSeed), 128);
    }
}
