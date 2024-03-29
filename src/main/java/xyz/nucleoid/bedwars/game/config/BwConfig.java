package xyz.nucleoid.bedwars.game.config;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import xyz.nucleoid.bedwars.game.active.modifiers.GameModifier;
import xyz.nucleoid.bedwars.game.generator.BwSkyMapConfig;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.plasmid.game.common.config.CombatConfig;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamList;

import java.util.Collections;
import java.util.List;

public record BwConfig(
        Identifier dimension,
        Either<BwSkyMapConfig, Identifier> map,
        CombatConfig combat,
        List<GameModifier> modifiers,
        GameTeamList teams,
        PlayerConfig players,
        boolean keepInventory,
        GeneratorConfig generatorConfig
) {
    public static final Codec<BwConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Identifier.CODEC.optionalFieldOf("dimension", Fantasy.DEFAULT_DIM_TYPE.getValue()).forGetter(BwConfig::dimension),
                Codec.either(BwSkyMapConfig.CODEC, Identifier.CODEC).fieldOf("map").forGetter(BwConfig::map),
                CombatConfig.CODEC.optionalFieldOf("combat", CombatConfig.DEFAULT).forGetter(BwConfig::combat),
                GameModifier.CODEC.listOf().optionalFieldOf("modifiers", Collections.emptyList()).forGetter(BwConfig::modifiers),
                GameTeamList.CODEC.fieldOf("teams").forGetter(BwConfig::teams),
                PlayerConfig.CODEC.fieldOf("players").forGetter(BwConfig::players),
                Codec.BOOL.optionalFieldOf("keep_inventory", false).forGetter(BwConfig::keepInventory),
                GeneratorConfig.CODEC.fieldOf("generator").forGetter(BwConfig::generatorConfig)
        ).apply(instance, BwConfig::new);
    });
}
