package net.gegy1000.bedwars.game.generator;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.game.map.template.TemplateChunkGenerator;
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
        // TODO
    }
}
