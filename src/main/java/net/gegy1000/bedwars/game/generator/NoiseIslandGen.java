package net.gegy1000.bedwars.game.generator;

import net.gegy1000.bedwars.game.generator.feature.TreeGen;
import net.gegy1000.gl.game.map.GameMapBuilder;
import net.gegy1000.gl.world.generator.OpenSimplexNoise;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

public final class NoiseIslandGen {
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.getDefaultState();
    private static final BlockState DIRT = Blocks.DIRT.getDefaultState();
    private static final BlockState STONE = Blocks.STONE.getDefaultState();

    private final BlockPos origin;
    private final OpenSimplexNoise noise;

    private int radius = 10;
    private int treeAmt = 0;
    private double noiseHorizontalFrequency = 1.0;
    private double noiseVerticalFrequency = 1.0;
    private boolean checkSpace = false;

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

    public void setCheckSpace(boolean checkSpace) {
        this.checkSpace = checkSpace;
    }

    public void setTreeAmt(int treeAmt) {
        this.treeAmt = treeAmt;
    }

    public void addTo(GameMapBuilder builder) {
        double noiseHorizontalFrequency = this.noiseHorizontalFrequency;
        double noiseVerticalFrequency = this.noiseVerticalFrequency;

        int radiusSquared = radius * radius;

        // if checkSpace is enabled, return if there is an obstruction
        if (checkSpace) {
            if (!checkSpace(builder)) {
                return;
            }
        }

        List<BlockPos> surfaceBlocks = new ArrayList<>();

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
                            builder.setBlockState(pos, STONE, false);
                        }
                    }
                }

                this.buildSurface(builder, -radius, radius + 1, x, z, surfaceBlocks);
            }
        }

        generateFeatures(builder, new Random(), surfaceBlocks);
    }

    private void buildSurface(GameMapBuilder builder, int minY, int maxY, int x, int z, List<BlockPos> surfaceBlocks) {
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
                    if (depth == 0) {
                        builder.setBlockState(herePos, GRASS, false);
                        surfaceBlocks.add(herePos.toImmutable());
                    } else {
                        builder.setBlockState(herePos, DIRT, false);
                    }
                }

                depth++;
            }

            wasAir = isAir;
        }
    }

    private void generateFeatures(GameMapBuilder builder, Random random, List<BlockPos> surfaceBlocks) {
        for (int i = 0; i < treeAmt; i++) {
            // Get a random pos
            BlockPos pos = surfaceBlocks.get(random.nextInt(surfaceBlocks.size()));
            new TreeGen(pos).addTo(builder);

            // Remove this position
            surfaceBlocks.remove(pos);
        }
    }

    private boolean checkSpace(GameMapBuilder builder) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius; y <= radius; y++) {
                    if (!builder.getBlockState(pos(x, y, z)).isAir()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private BlockPos pos(int x, int y, int z) {
        return this.mutablePos.set(this.origin.getX() + x, this.origin.getY() + y, this.origin.getZ() + z);
    }
}
