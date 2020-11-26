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
            Codec.BOOL.fieldOf("check_space").forGetter(generator -> generator.checkSpace),
            Codec.INT.optionalFieldOf("diamond_ore_chance", -1).forGetter(generator -> generator.diamondOreChance),
            Codec.INT.optionalFieldOf("gold_ore_chance", -1).forGetter(generator -> generator.goldOreChance)
    ).apply(instance, NoiseIslandConfig::new));

    public final int radius;
    public final double falloffMultiplier;
    public final double falloffStrength;
    public final double falloffOffset;
    public final double noiseHorizontalFrequency;
    public final double noiseVerticalFrequency;
    public final boolean checkSpace;
    public final int diamondOreChance;
    public final int goldOreChance;

    public NoiseIslandConfig(int radius, double falloffMultiplier, double falloffStrength, double falloffOffset, double noiseHorizontalFrequency, double noiseVerticalFrequency, boolean checkSpace, int diamondOreChance, int goldOreChance) {
        this.radius = radius;
        this.falloffMultiplier = falloffMultiplier;
        this.falloffStrength = falloffStrength;
        this.falloffOffset = falloffOffset;
        this.noiseHorizontalFrequency = noiseHorizontalFrequency;
        this.noiseVerticalFrequency = noiseVerticalFrequency;
        this.checkSpace = checkSpace;
        this.diamondOreChance = diamondOreChance;
        this.goldOreChance = goldOreChance;
    }

    public NoiseIslandGenerator createGenerator(BlockPos origin, long seed) {
        return new NoiseIslandGenerator(this, origin, new OpenSimplexNoise(seed));
    }
}
