package net.gegy1000.bedwars.game.generator.island;

import net.gegy1000.bedwars.game.BwMap;
import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.util.BlockBounds;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

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
