package xyz.nucleoid.bedwars.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GeneratorConfig(GeneratorLevelConfig level1, GeneratorLevelConfig level2, GeneratorLevelConfig level3, int diamond, long diamondSpawnInterval, int emerald, long emeraldSpawnInterval) {
    public static final Codec<GeneratorConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GeneratorLevelConfig.CODEC.fieldOf("level_1").forGetter(GeneratorConfig::level1),
            GeneratorLevelConfig.CODEC.fieldOf("level_2").forGetter(GeneratorConfig::level2),
            GeneratorLevelConfig.CODEC.fieldOf("level_3").forGetter(GeneratorConfig::level3),
            Codec.INT.fieldOf("diamond").forGetter(GeneratorConfig::diamond),
            Codec.LONG.fieldOf("diamondSpawnInterval").forGetter(GeneratorConfig::diamondSpawnInterval),
            Codec.INT.fieldOf("emerald").forGetter(GeneratorConfig::emerald),
            Codec.LONG.fieldOf("emeraldSpawnInterval").forGetter(GeneratorConfig::emeraldSpawnInterval)
    ).apply(instance, GeneratorConfig::new));

    public static record GeneratorLevelConfig(int ironSpawnRate, int goldSpawnRate, int emeraldSpawnRate, int diamondSpawnRate, long spawnIntervalTicks) {
        public static final Codec<GeneratorLevelConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("iron").forGetter(GeneratorLevelConfig::ironSpawnRate),
                Codec.INT.fieldOf("gold").forGetter(GeneratorLevelConfig::goldSpawnRate),
                Codec.INT.optionalFieldOf("emerald", 0).forGetter(GeneratorLevelConfig::emeraldSpawnRate),
                Codec.INT.optionalFieldOf("diamond", 0).forGetter(GeneratorLevelConfig::diamondSpawnRate),
                Codec.LONG.fieldOf("spawnIntervalTicks").forGetter(GeneratorLevelConfig::spawnIntervalTicks)
        ).apply(instance, GeneratorLevelConfig::new));
    }
}
