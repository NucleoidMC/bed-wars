package xyz.nucleoid.bedwars.game.generator.island;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kdotjpg.opensimplex.OpenSimplexNoise;
import net.minecraft.util.math.BlockPos;

public final class NoiseIslandConfig {
    public static final Codec<NoiseIslandConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
            Codec.INT.optionalFieldOf("ore_chance", -1).forGetter(generator -> generator.oreChance)
    ).apply(instance, NoiseIslandConfig::new));

    public final int radius;
    public final double falloffMultiplier;
    public final double falloffStrength;
    public final double falloffOffset;
    public final double noiseHorizontalFrequency;
    public final double noiseVerticalFrequency;
    public final int treeAmt;
    public final int treeAmtRand;
    public final int treeExtraAmtChance;
    public final int grassAmt;
    public final boolean checkSpace;
    public final int oreChance;

    public NoiseIslandConfig(int radius, double falloffMultiplier, double falloffStrength, double falloffOffset, double noiseHorizontalFrequency, double noiseVerticalFrequency, int treeAmt, int treeAmtRand, int treeExtraAmtChance, int grassAmt, boolean checkSpace, int oreChance) {
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
        this.oreChance = oreChance;
    }

    public NoiseIslandGenerator createGenerator(BlockPos origin, long seed) {
        return new NoiseIslandGenerator(this, origin, new OpenSimplexNoise(seed));
    }
}
