package xyz.nucleoid.bedwars.game.generator.island;

import kdotjpg.opensimplex.OpenSimplexNoise;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.Random;

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
        double freq = 1.0 / (radius / 2.0);

        BlockState state;
        Random random = new Random();

        // Check space if enabled
        if (config.checkSpace) {
            for (BlockPos pos : this.bounds) {
                // Don't generate if there is something blocking here
                if (!template.getBlockState(pos).isAir()) {
                    return;
                }
            }
        }

        for (BlockPos pos : this.bounds) {
            state = Blocks.STONE.getDefaultState();

            double localX = ((double)(pos.getX() - origin.getX())) / radius;
            double localY = ((double)(pos.getY() - origin.getY())) / radius;
            double localZ = ((double)(pos.getZ() - origin.getZ())) / radius;
            double distance2 = localX * localX + localY * localY + localZ * localZ;

            double shapeNoise = noiseSampler.eval(pos.getX() * freq, pos.getY() * freq, pos.getZ() * freq) * 0.5;

            // Place stone based on noise
            if (distance2 <= 1 + shapeNoise) {
                double noiseX = pos.getX() * noiseHorizontalFrequency;
                double noiseY = pos.getY() * noiseVerticalFrequency;
                double noiseZ = pos.getZ() * noiseHorizontalFrequency;

                double noise = noiseSampler.eval(noiseX, noiseY, noiseZ);
                noise += this.computeNoiseFalloff(pos.getY() - origin.getY());

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
