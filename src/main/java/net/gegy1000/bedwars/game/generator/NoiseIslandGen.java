package net.gegy1000.bedwars.game.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kdotjpg.opensimplex.OpenSimplexNoise;
import net.gegy1000.bedwars.game.generator.feature.AspenTreeGen;
import net.gegy1000.bedwars.game.generator.feature.GrassGen;
import net.gegy1000.bedwars.game.generator.feature.TreeGen;
import net.gegy1000.gl.game.map.GameMapBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

public final class NoiseIslandGen {
    public static final Codec<NoiseIslandGen> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(generator -> generator.radius),
            Codec.DOUBLE.fieldOf("falloff_multiplier").forGetter(generator -> generator.falloffMultiplier),
            Codec.DOUBLE.fieldOf("falloff_strength").forGetter(generator -> generator.falloffStrength),
            Codec.DOUBLE.fieldOf("falloff_offset").forGetter(generator -> generator.falloffOffset),
            Codec.DOUBLE.fieldOf("horizontal_frequency").forGetter(generator -> generator.noiseHorizontalFrequency),
            Codec.DOUBLE.fieldOf("vertical_frequency").forGetter(generator -> generator.noiseVerticalFrequency),
            Codec.INT.fieldOf("tree_amt").forGetter(generator -> generator.treeAmt),
            Codec.INT.fieldOf("tree_amt_rand").forGetter(generator -> generator.treeAmtRand),
            Codec.INT.fieldOf("tree_extra_amt_chance").forGetter(generator -> generator.treeAmtRand),
            Codec.INT.fieldOf("grass_amt").forGetter(generator -> generator.grassAmt),
            Codec.BOOL.fieldOf("check_space").forGetter(generator -> generator.checkSpace),
            //TODO: refactor to biomes
            Codec.BOOL.fieldOf("use_aspen_trees").forGetter(generator -> generator.useAspenTrees)
    ).apply(instance, NoiseIslandGen::new));
    private static final BlockState GRASS = Blocks.GRASS_BLOCK.getDefaultState();
    private static final BlockState DIRT = Blocks.DIRT.getDefaultState();
    private static final BlockState STONE = Blocks.STONE.getDefaultState();

    private BlockPos origin;
    private OpenSimplexNoise noise;

    private final int radius;
    private final double falloffMultiplier;
    private final double falloffStrength;
    private final double falloffOffset;
    private final double noiseHorizontalFrequency;
    private final double noiseVerticalFrequency;
    private final int treeAmt;
    private final int treeAmtRand;
    private final int treeExtraAmtChance;
    private final int grassAmt;
    private final boolean checkSpace;
    private final boolean useAspenTrees;

    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    public NoiseIslandGen(int radius, double falloffMultiplier, double falloffStrength, double falloffOffset, double noiseHorizontalFrequency, double noiseVerticalFrequency, int treeAmt, int treeAmtRand, int treeExtraAmtChance, int grassAmt, boolean checkSpace, boolean useAspenTrees) {
        this.radius = radius;
        this.falloffMultiplier = falloffMultiplier;
        this.falloffStrength = falloffStrength;
        this.falloffOffset = falloffOffset;
        this.noiseHorizontalFrequency = noiseHorizontalFrequency;
        this.noiseVerticalFrequency = noiseVerticalFrequency;
        this.treeAmt = treeAmt;
        this.treeAmtRand = treeAmtRand;
        this.treeExtraAmtChance = treeExtraAmtChance;
        this.grassAmt = grassAmt;
        this.checkSpace = checkSpace;
        this.useAspenTrees = useAspenTrees;
    }

    public void setOrigin(BlockPos origin) {
        this.origin = origin;
    }

    public void setNoise(OpenSimplexNoise noise) {
        this.noise = noise;
    }

    public int getRadius() {
        return radius;
    }

    public double computeNoiseFalloff(int y) {
        return (falloffMultiplier * (falloffStrength / (y + radius))) + falloffOffset;
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
                        noise += computeNoiseFalloff(y);

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
        // Calculate the final tree amount for this island

        int finalTreeAmt = treeAmt + random.nextInt(treeAmtRand + 1);
        if (treeExtraAmtChance > 0) {
            if (random.nextInt(treeExtraAmtChance) == 0) {
                finalTreeAmt++;
            }
        }

        for (int i = 0; i < finalTreeAmt; i++) {
            if (surfaceBlocks.size() == 0) break;

            // Get a random pos
            BlockPos pos = surfaceBlocks.get(random.nextInt(surfaceBlocks.size()));
            if (useAspenTrees) {
                new AspenTreeGen(pos.up()).addTo(builder);
            } else {
                new TreeGen(pos.up()).addTo(builder);
            }

            // Remove this position
            surfaceBlocks.remove(pos);
        }

        for (int i = 0; i < grassAmt; i++) {
            if (surfaceBlocks.size() == 0) break;

            // Get a random pos
            BlockPos pos = surfaceBlocks.get(random.nextInt(surfaceBlocks.size()));
            new GrassGen(pos).addTo(builder);

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
