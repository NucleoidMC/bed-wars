package net.gegy1000.bedwars.game.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.bedwars.game.generator.island.NoiseIslandConfig;

public final class BwSkyMapConfig {
    public static final Codec<BwSkyMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NoiseIslandConfig.CODEC.fieldOf("diamond_generator").forGetter(generator -> generator.diamondGenerator),
            NoiseIslandConfig.CODEC.fieldOf("center_generator").forGetter(generator -> generator.centerGenerator),
            NoiseIslandConfig.CODEC.fieldOf("small_island_generator").forGetter(generator -> generator.smallIslandGenerator),
            Codec.DOUBLE.fieldOf("spawn_island_distance").forGetter(generator -> generator.spawnIslandDistance),
            Codec.DOUBLE.fieldOf("diamond_island_distance").forGetter(generator -> generator.diamondIslandDistance),
            Codec.INT.fieldOf("small_island_count").forGetter(generator -> generator.smallIslandCount),
            Codec.INT.fieldOf("small_island_horizontal_spread").forGetter(generator -> generator.smallIslandHorizontalSpread),
            Codec.INT.fieldOf("small_island_vertical_spread").forGetter(generator -> generator.smallIslandVerticalSpread),
            Codec.INT.fieldOf("small_island_cutoff").forGetter(generator -> generator.smallIslandCutoff)
    ).apply(instance, BwSkyMapConfig::new));

    public final NoiseIslandConfig diamondGenerator;
    public final NoiseIslandConfig centerGenerator;
    public final NoiseIslandConfig smallIslandGenerator;
    public final double spawnIslandDistance;
    public final double diamondIslandDistance;
    public final int smallIslandCount;
    public final int smallIslandHorizontalSpread;
    public final int smallIslandVerticalSpread;
    public final int smallIslandCutoff;

    private BwSkyMapConfig(NoiseIslandConfig diamondGenerator, NoiseIslandConfig centerGenerator, NoiseIslandConfig smallIslandGenerator, double spawnIslandDistance, double diamondIslandDistance, int smallIslandCount, int smallIslandHorizontalSpread, int smallIslandVerticalSpread, int smallIslandCutoff) {
        this.diamondGenerator = diamondGenerator;
        this.centerGenerator = centerGenerator;
        this.smallIslandGenerator = smallIslandGenerator;
        this.spawnIslandDistance = spawnIslandDistance;
        this.diamondIslandDistance = diamondIslandDistance;
        this.smallIslandCount = smallIslandCount;
        this.smallIslandHorizontalSpread = smallIslandHorizontalSpread;
        this.smallIslandVerticalSpread = smallIslandVerticalSpread;
        this.smallIslandCutoff = smallIslandCutoff;
    }
}
