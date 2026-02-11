package xyz.nucleoid.bedwars.game.generator.island;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public final class BwDiamondIsland {
    private final NoiseIslandConfig config;
    private final BlockPos origin;

    public BwDiamondIsland(NoiseIslandConfig config, BlockPos origin) {
        this.config = config;
        this.origin = origin;
    }

    public void addTo(BwMap map, MapTemplate template, long seed) {
        NoiseIslandGenerator island = this.config.createGenerator(this.origin, seed);
        island.addTo(template);

        this.addDiamondSpawner(map, template);
    }

    private void addDiamondSpawner(BwMap map, MapTemplate template) {
        BlockPos surfacePos = template.getTopPos(this.origin.getX(), this.origin.getZ(), Heightmap.Types.WORLD_SURFACE_WG);

        template.setBlockState(surfacePos, Blocks.DIAMOND_BLOCK.defaultBlockState());
        map.addDiamondGenerator(BlockBounds.ofBlock(surfacePos.above()));
        map.addProtectedBlock(surfacePos.asLong());
    }
}
