package net.gegy1000.bedwars.game.generator.island;

import java.util.Random;

import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.bedwars.game.generator.NoiseIslandGen;
import net.gegy1000.gl.game.map.GameMapBuilder;
import net.gegy1000.gl.game.map.GameRegion;
import net.gegy1000.gl.world.BlockBounds;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class CenterIslandGen implements MapGen {
    private final NoiseIslandGen noiseGen;
    private final BlockPos origin;

    public CenterIslandGen(BlockPos origin, long seed) {
        this.origin = origin;
        this.noiseGen = new NoiseIslandGen(origin, seed);
        this.noiseGen.setRadius(20);
        this.noiseGen.setNoiseFrequency(1.0 / 12.0, 1.0 / 16.0);
        this.noiseGen.setFalloff(CenterIslandGen::computeNoiseFalloff);
        this.noiseGen.setTreeAmt(3 + new Random(seed).nextInt(4));
    }

    @Override
    public void addTo(GameMapBuilder builder) {
        this.noiseGen.addTo(builder);
        addRegionsTo(builder);
    }

    @Override
    public void addRegionsTo(GameMapBuilder builder) {
        BlockPos start = origin;
        for (int i = 0; i < 20; i++) {
            if (builder.getBlockState(origin.up(i)) == Blocks.GRASS_BLOCK.getDefaultState()) {
                start = origin.up(i);
                break;
            }
        }

        builder.setBlockState(start, Blocks.EMERALD_BLOCK.getDefaultState());
        builder.addRegion(new GameRegion("emerald_spawn", new BlockBounds(
                start.up()
        )));
    }

    // Desmos: 0.75\cdot\frac{90}{x+20}-3.4
    private static double computeNoiseFalloff(double y) {
        return (0.75 * (90.0 / (y + 20))) - 3.4;
    }
}
