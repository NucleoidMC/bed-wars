package xyz.nucleoid.bedwars.game.generator;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import xyz.nucleoid.bedwars.game.BwMap;
import xyz.nucleoid.bedwars.game.generator.theme.MapTheme;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

import java.util.Random;

public final class BwSkyChunkGenerator extends TemplateChunkGenerator {
    private final BwMap map;
    private final BwSkyMapConfig config;

    public BwSkyChunkGenerator(BwMap map, BwSkyMapConfig config, MinecraftServer server, MapTemplate template) {
        super(server, template);
        this.map = map;
        this.config = config;
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();

        int minWorldX = chunkPos.getStartX();
        int minWorldZ = chunkPos.getStartZ();
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        MapTheme theme = this.config.theme;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int height = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, x, z);

                mutablePos.set(minWorldX + x, height, minWorldZ + z);

                for (int y = height; y >= 0; y--) {
                    mutablePos.setY(y);

                    if (region.getBlockState(mutablePos).isAir()) {
                        height = y - 1;
                    }

                    if (region.getBlockState(mutablePos).isOf(Blocks.STONE)) {
                        if (y == height) {
                            region.setBlockState(mutablePos, theme.topState(), 3);
                        } else if (height - y <= 4) {
                            region.setBlockState(mutablePos, theme.middleState(), 3);
                        } else {
                            region.setBlockState(mutablePos, theme.stoneState(), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        Random random = new Random();
        MapTheme theme = this.config.theme;

        var centerPos = chunk.getPos();
        for (int i = 0; i < theme.treeAmt(); i++) {
            int x = centerPos.getStartX() + random.nextInt(16);
            int z = centerPos.getStartZ() + random.nextInt(16);
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z);

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
            int x = centerPos.getStartX() + random.nextInt(16);
            int z = centerPos.getStartZ() + random.nextInt(16);
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z);

            theme.grass().generate(world, new BlockPos(x, y, z), random);
        }
    }
}
