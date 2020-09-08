package xyz.nucleoid.bedwars.game.generator.island;

import java.util.Random;

import kdotjpg.opensimplex.OpenSimplexNoise;
import xyz.nucleoid.plasmid.game.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;
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

        BlockState state;
        Random random = new Random();

        // Check space if enabled
        if (config.checkSpace) {
            for (BlockPos pos : this.bounds.iterate()) {
                // Don't generate if there is something blocking here
                if (!template.getBlockState(pos).isAir()) {
                    return;
                }
            }
        }

        for (BlockPos pos : this.bounds.iterate()) {
            state = Blocks.STONE.getDefaultState();

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

                if (config.goldOreChance > 1 && random.nextInt(config.goldOreChance) == 0) {
                    state = Blocks.GOLD_ORE.getDefaultState();
                }

                if (config.diamondOreChance > 1 && random.nextInt(config.diamondOreChance) == 0) {
                    state = Blocks.DIAMOND_ORE.getDefaultState();
                }

                if (noise > 0) {
                    template.setBlockState(pos, state);
                }
            }
        }
    }

    private double computeNoiseFalloff(int y) {
        NoiseIslandConfig config = this.config;
        return (config.falloffMultiplier * (config.falloffStrength / (y + config.radius))) + config.falloffOffset;
    }
}
