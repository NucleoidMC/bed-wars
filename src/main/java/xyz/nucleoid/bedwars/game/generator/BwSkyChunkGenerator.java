package xyz.nucleoid.bedwars.game.generator;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.RandomState;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.generator.theme.MapTheme;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

public final class BwSkyChunkGenerator extends TemplateChunkGenerator {
    private final BwMap map;
    private final BwSkyMapConfig config;

    public BwSkyChunkGenerator(BwMap map, BwSkyMapConfig config, MinecraftServer server, MapTemplate template) {
        super(server, template);
        this.map = map;
        this.config = config;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState noiseConfig, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();

        int minWorldX = chunkPos.getMinBlockX();
        int minWorldZ = chunkPos.getMinBlockZ();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        MapTheme theme = this.config.theme;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

                mutablePos.set(minWorldX + x, height, minWorldZ + z);

                for (int y = height; y >= 0; y--) {
                    mutablePos.setY(y);

                    if (region.getBlockState(mutablePos).isAir()) {
                        height = y - 1;
                    }

                    if (region.getBlockState(mutablePos).is(Blocks.STONE)) {
                        if (y == height) {
                            region.setBlock(mutablePos, theme.topState(), 3);
                        } else if (height - y <= 4) {
                            region.setBlock(mutablePos, theme.middleState(), 3);
                        } else {
                            region.setBlock(mutablePos, theme.stoneState(), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel world, ChunkAccess chunk, StructureManager structureAccessor) {
        RandomSource random = RandomSource.createNewThreadLocalInstance();
        MapTheme theme = this.config.theme;

        var centerPos = chunk.getPos();
        for (int i = 0; i < theme.treeAmt(); i++) {
            int x = centerPos.getMinBlockX() + random.nextInt(16);
            int z = centerPos.getMinBlockZ() + random.nextInt(16);
            int y = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

            boolean generate = true;
            for (BwMap.TeamRegions regions : this.map.getAllTeamRegions().values()) {
                if (regions.base().contains(x, z)) {
                    generate = false;
                    break;
                }
            }

            if (generate) {
                theme.tree().generate(world, new BlockPos(x, y, z), random);
            }
        }

        for (int i = 0; i < theme.grassAmt(); i++) {
            int x = centerPos.getMinBlockX() + random.nextInt(16);
            int z = centerPos.getMinBlockZ() + random.nextInt(16);
            int y = world.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

            theme.grass().generate(world, new BlockPos(x, y, z), random);
        }
    }
}
