package xyz.nucleoid.bedwars.game.generator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import xyz.nucleoid.bedwars.game.generator.feature.PoplarTreeFeature;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.game.map.template.TemplateChunkGenerator;

import java.util.Random;

public final class BwSkyChunkGenerator extends TemplateChunkGenerator {
    private static final BlockState STONE = Blocks.STONE.getDefaultState();
    private static final BlockState WATER = Blocks.WATER.getDefaultState();

    public BwSkyChunkGenerator(MapTemplate template) {
        super(template, BlockPos.ORIGIN);
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

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = minWorldX + x;
                int worldZ = minWorldZ + z;
                int height = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, x, z) + 1;

                mutablePos.set(minWorldX + x, height, minWorldZ + z);
                Biomes.PLAINS.buildSurface(chunkRandom, chunk, worldX, worldZ, height, 0.0, STONE, WATER, 0, seed);
            }
        }
    }

    @Override
    public void generateFeatures(ChunkRegion region, StructureAccessor structures) {
        // So this somehow works without registering the features but we *probably* should...
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            int x = (region.getCenterChunkX() * 16) + random.nextInt(16);
            int z = (region.getCenterChunkZ() * 16) + random.nextInt(16);
            int y = region.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z);

            PoplarTreeFeature.INSTANCE.generate(region, this, random, new BlockPos(x, y, z), DefaultFeatureConfig.DEFAULT);
        }

        for (int i = 0; i < 4; i++) {
            int x = (region.getCenterChunkX() * 16) + random.nextInt(16);
            int z = (region.getCenterChunkZ() * 16) + random.nextInt(16);
            int y = region.getTopY(Heightmap.Type.WORLD_SURFACE_WG, x, z);

            Feature.RANDOM_PATCH.generate(region, this, random, new BlockPos(x, y, z), ConfiguredFeatures.Configs.GRASS_CONFIG);
        }
    }
}
