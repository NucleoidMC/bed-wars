package net.gegy1000.bedwars.game.generator.island;

import kdotjpg.opensimplex.OpenSimplexNoise;
import net.gegy1000.bedwars.game.generator.MapGen;
import net.gegy1000.bedwars.game.generator.NoiseIslandGen;
import net.gegy1000.plasmid.game.map.GameMapBuilder;
import net.gegy1000.plasmid.game.map.GameRegion;
import net.gegy1000.plasmid.world.BlockBounds;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
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
        // TODO: scale with team count
        Direction[] horizontals = new Direction[] { Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST };

        for (Direction horizontal : horizontals) {
            this.addEmeraldSpawner(builder, this.origin.offset(horizontal, 8));
        }
    }

    private void addEmeraldSpawner(GameMapBuilder builder, BlockPos pos) {
        int y = builder.getTopY(Heightmap.Type.MOTION_BLOCKING, pos);
        BlockPos start = new BlockPos(pos.getX(), y, pos.getZ());

        builder.setBlockState(start, Blocks.EMERALD_BLOCK.getDefaultState());
        builder.addRegion(new GameRegion("emerald_spawn", new BlockBounds(start.up(2))));
    }
}
