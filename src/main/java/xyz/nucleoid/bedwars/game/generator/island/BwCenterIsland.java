package xyz.nucleoid.bedwars.game.generator.island;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public final class BwCenterIsland {
    private final NoiseIslandConfig config;
    private final BlockPos origin;

    public BwCenterIsland(NoiseIslandConfig config, BlockPos origin) {
        this.config = config;
        this.origin = origin;
    }

    public void addTo(BwMap map, MapTemplate template, long seed, int teamAmount, double emeraldDistance) {
        NoiseIslandGenerator generator = this.config.createGenerator(this.origin, seed);
        generator.addTo(template);

        for (int i = 0; i < teamAmount; i++) {
            double theta = ((double) i / teamAmount) * (2 * Math.PI);
            double x = Math.cos(theta) * emeraldDistance;
            double z = Math.sin(theta) * emeraldDistance;

            this.addEmeraldSpawner(map, template, this.origin.add(MathHelper.floor(x), 0, MathHelper.floor(z)));
        }

        this.addCenterSpawn(map, template);
    }

    private void addEmeraldSpawner(BwMap map, MapTemplate template, BlockPos pos) {
        BlockPos surfacePos = template.getTopPos(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG);

        template.setBlockState(surfacePos, Blocks.EMERALD_BLOCK.getDefaultState());
        map.addEmeraldGenerator(BlockBounds.ofBlock(surfacePos.up()));
        map.addProtectedBlock(surfacePos.asLong());
    }

    private void addCenterSpawn(BwMap map, MapTemplate template) {
        BlockPos surfacePos = template.getTopPos(this.origin.getX(), this.origin.getZ(), Heightmap.Type.WORLD_SURFACE_WG);
        map.setCenterSpawn(surfacePos.up());
    }
}
