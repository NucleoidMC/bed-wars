package net.gegy1000.bedwars.game.bw.gen;

import net.gegy1000.bedwars.map.GameMapBuilder;
import net.gegy1000.bedwars.util.OpenSimplexNoise;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.function.DoubleUnaryOperator;

public final class NoiseIslandGen {
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.getDefaultState();
    private static final BlockState DIRT = Blocks.DIRT.getDefaultState();
    private static final BlockState STONE = Blocks.STONE.getDefaultState();

    private final BlockPos origin;
    private final OpenSimplexNoise noise;

    private int radius = 10;
    private double noiseHorizontalFrequency = 1.0;
    private double noiseVerticalFrequency = 1.0;

    private DoubleUnaryOperator falloffFunction = y -> y;

    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    public NoiseIslandGen(BlockPos origin, long seed) {
        this.origin = origin;
        this.noise = new OpenSimplexNoise(seed);
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setNoiseFrequency(double horizontal, double vertical) {
        this.noiseHorizontalFrequency = horizontal;
        this.noiseVerticalFrequency = vertical;
    }

    public void setFalloff(DoubleUnaryOperator falloffFunction) {
        this.falloffFunction = falloffFunction;
    }

    public void addTo(GameMapBuilder builder) {
        int radius = this.radius;
        double noiseHorizontalFrequency = this.noiseHorizontalFrequency;
        double noiseVerticalFrequency = this.noiseVerticalFrequency;

        int radiusSquared = radius * radius;

        // Iterate a circle
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {

                // Populate Noise
                for (int y = -radius; y <= radius; y++) {
                    int squareDistance = (x * x) + (y * y) + (z * z);

                    // Place stone based on noise
                    if (squareDistance <= radiusSquared) {
                        BlockPos pos = this.pos(x, y, z);

                        double noiseX = pos.getX() * noiseHorizontalFrequency;
                        double noiseY = pos.getY() * noiseVerticalFrequency;
                        double noiseZ = pos.getZ() * noiseHorizontalFrequency;

                        double noise = this.noise.eval(noiseX, noiseY, noiseZ);
                        noise += this.falloffFunction.applyAsDouble(y);

                        if (noise > 0) {
                            builder.setBlockState(pos, STONE);
                        }
                    }
                }

                this.buildSurface(builder, -radius, radius + 1, x, z);
            }
        }
    }

    private void buildSurface(GameMapBuilder builder, int minY, int maxY, int x, int z) {
        boolean wasAir = false;
        int depth = -1;

        for (int y = maxY; y > minY; y--) {
            BlockPos herePos = this.pos(x, y, z);
            BlockState hereState = builder.getBlockState(herePos);

            boolean isAir = hereState.isAir();
            if (!isAir) {
                if (wasAir) {
                    // if we're transitioning from air to not air, reset depth
                    depth = 0;
                }

                // replace stone with soil until depth=3
                if (depth < 3 && hereState == STONE) {
                    builder.setBlockState(herePos, depth == 0 ? GRASS : DIRT);
                }

                depth++;
            }

            wasAir = isAir;
        }
    }

    private BlockPos pos(int x, int y, int z) {
        return this.mutablePos.set(this.origin.getX() + x, this.origin.getY() + y, this.origin.getZ() + z);
    }
}
