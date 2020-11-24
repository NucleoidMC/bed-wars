package xyz.nucleoid.bedwars.game.generator.island;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;

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
        BlockPos surfacePos = template.getTopPos(this.origin.getX(), this.origin.getZ(), Heightmap.Type.WORLD_SURFACE_WG);

        template.setBlockState(surfacePos, Blocks.DIAMOND_BLOCK.getDefaultState());
        map.addDiamondGenerator(BlockBounds.of(surfacePos));
    }
}
