package xyz.nucleoid.bedwars.game.generator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;

import xyz.nucleoid.bedwars.game.generator.theme.MapTheme;
import xyz.nucleoid.plasmid.game.gen.feature.GrassGen;
import xyz.nucleoid.plasmid.game.gen.feature.tree.PoplarTreeGen;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;

import java.util.Random;

public final class BwSkyChunkGenerator extends TemplateChunkGenerator {
    private final BwSkyMapConfig config;

    public BwSkyChunkGenerator(BwSkyMapConfig config, MinecraftServer server, MapTemplate template) {
        super(server, template);
        this.config = config;
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();

        ChunkRandom chunkRandom = new ChunkRandom();
        chunkRandom.setTerrainSeed(chunkPos.x, chunkPos.z);
        long seed = region.getSeed();

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
    public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Random random = new Random();
        MapTheme theme = this.config.theme;
        
        for (int i = 0; i < theme.treeAmt(); i++) {
            int x = (region.getCenterChunkX() * 16) + random.nextInt(16);
            int z = (region.getCenterChunkZ() * 16) + random.nextInt(16);
            int y = region.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z);

            theme.tree().generate(region, mutable.set(x, y, z).toImmutable(), random);
        }

        for (int i = 0; i < theme.grassAmt(); i++) {
            int x = (region.getCenterChunkX() * 16) + random.nextInt(16);
            int z = (region.getCenterChunkZ() * 16) + random.nextInt(16);
            int y = region.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z);

            theme.grass().generate(region, mutable.set(x, y, z).toImmutable(), random);
        }
    }
}
