package net.gegy1000.bedwars.game.generator.island;

import kdotjpg.opensimplex.OpenSimplexNoise;
import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.bedwars.game.generator.NoiseIslandGen;
import net.gegy1000.gl.game.map.GameMapBuilder;
import net.gegy1000.gl.game.map.GameRegion;
import net.gegy1000.gl.world.BlockBounds;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

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
        int y = builder.getTopY(Heightmap.Type.MOTION_BLOCKING, this.origin);
        BlockPos start = new BlockPos(this.origin.getX(), y, this.origin.getZ());

        builder.setBlockState(start, Blocks.EMERALD_BLOCK.getDefaultState());
        builder.addRegion(new GameRegion("emerald_spawn", new BlockBounds(start.up())));
    }
}
