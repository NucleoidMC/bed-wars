package net.gegy1000.bedwars.game.generator.island;

import kdotjpg.opensimplex.OpenSimplexNoise;
import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.plasmid.util.BlockBounds;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public final class NoiseIslandGenerator {
    private final NoiseIslandConfig config;
    private final BlockPos origin;
    private final BlockBounds bounds;
    private final OpenSimplexNoise noise;

    public NoiseIslandGenerator(NoiseIslandConfig config, BlockPos origin, OpenSimplexNoise noise) {
        this.config = config;
        this.origin = origin;
        this.bounds = new BlockBounds(
                origin.add(-config.radius, -config.radius, -config.radius),
                origin.add(config.radius, config.radius, config.radius)
        );
        this.noise = noise;
    }

    public void addTo(MapTemplate template) {
        BlockPos origin = this.origin;
        OpenSimplexNoise noiseSampler = this.noise;

        double noiseHorizontalFrequency = this.config.noiseHorizontalFrequency;
        double noiseVerticalFrequency = this.config.noiseVerticalFrequency;

        int radius = this.config.radius;
        int radius2 = radius * radius;

        BlockState stone = Blocks.STONE.getDefaultState();

        for (BlockPos pos : this.bounds.iterate()) {
            int localX = pos.getX() - origin.getX();
            int localY = pos.getY() - origin.getY();
            int localZ = pos.getZ() - origin.getZ();
            int distance2 = localX * localX + localY * localY + localZ * localZ;

            // Place stone based on noise
            if (distance2 <= radius2) {
                double noiseX = localX * noiseHorizontalFrequency;
                double noiseY = localY * noiseVerticalFrequency;
                double noiseZ = localZ * noiseHorizontalFrequency;

                double noise = noiseSampler.eval(noiseX, noiseY, noiseZ);
                noise += this.computeNoiseFalloff(localY);

                if (noise > 0) {
                    template.setBlockState(pos, stone);
                }
            }
        }
    }

    private double computeNoiseFalloff(int y) {
        NoiseIslandConfig config = this.config;
        return (config.falloffMultiplier * (config.falloffStrength / (y + config.radius))) + config.falloffOffset;
    }
}
