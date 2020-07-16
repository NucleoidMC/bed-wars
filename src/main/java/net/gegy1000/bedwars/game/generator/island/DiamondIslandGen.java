package net.gegy1000.bedwars.game.generator.island;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.bedwars.game.generator.NoiseIslandGen;
import net.gegy1000.gl.game.map.GameMapBuilder;
import net.gegy1000.gl.game.map.GameRegion;
import net.gegy1000.gl.world.BlockBounds;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class DiamondIslandGen implements MapGen {
    private final NoiseIslandGen noiseGen;
    private final BlockPos origin;

    public DiamondIslandGen(BlockPos origin, long seed) {
        this.origin = origin;
        this.noiseGen = new NoiseIslandGen(origin, seed);
        this.noiseGen.setRadius(10);
        this.noiseGen.setNoiseFrequency(1.0 / 8.0, 1.0 / 12.0);
        this.noiseGen.setFalloff(DiamondIslandGen::computeNoiseFalloff);
    }

    @Override
    public void addTo(GameMapBuilder builder) {
        this.noiseGen.addTo(builder);
        addRegionsTo(builder);
    }

    @Override
    public void addRegionsTo(GameMapBuilder builder) {
        BlockPos start = origin;
        for (int i = 0; i < 10; i++) {
            if (builder.getBlockState(origin.up(i)) == Blocks.GRASS_BLOCK.getDefaultState()) {
                start = origin.up(i);
                break;
            }
        }

        builder.setBlockState(start, Blocks.DIAMOND_BLOCK.getDefaultState());
        builder.addRegion(new GameRegion("diamond_spawn", new BlockBounds(
                start.up()
        )));
    }

    // Desmos: \frac{40}{x+10}-4.25
    private static double computeNoiseFalloff(double y) {
        return (40.0 / (y + 10)) - 4.25;
    }
}
