package net.gegy1000.bedwars.game.generator.island;

import java.util.Random;

import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.bedwars.game.generator.NoiseIslandGen;
import net.gegy1000.gl.game.map.GameMapBuilder;
import net.gegy1000.gl.game.map.GameRegion;
import net.gegy1000.gl.world.BlockBounds;
import net.gegy1000.gl.world.generator.OpenSimplexNoise;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class CenterIslandGen implements MapGen {
    private final NoiseIslandGen generator;
    private final BlockPos origin;
    private final long seed;

    public CenterIslandGen(NoiseIslandGen generator, BlockPos origin, long seed) {
        this.generator = generator;
        this.origin = origin;
        this.seed = seed;
    }

    @Override
    public void addTo(GameMapBuilder builder) {
        this.generator.setOrigin(origin);
        this.generator.setNoise(new OpenSimplexNoise(seed));
        this.generator.addTo(builder);
        addRegionsTo(builder);
    }

    @Override
    public void addRegionsTo(GameMapBuilder builder) {
        BlockPos start = origin;
        for (int i = 0; i < generator.getRadius(); i++) {
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
}
