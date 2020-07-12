package net.gegy1000.bedwars.game.bw;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.bedwars.game.GameTeam;
import net.gegy1000.bedwars.game.config.CombatConfig;
import net.gegy1000.bedwars.game.config.GameConfig;
import net.gegy1000.bedwars.game.config.GameMapConfig;
import net.gegy1000.bedwars.game.modifier.GameModifier;

import java.util.Collections;
import java.util.List;

public final class BedWarsConfig implements GameConfig {
    public static final Codec<BedWarsConfig> CODEC = RecordCodecBuilder.create(instance -> {
        Codec<GameMapConfig<BedWarsConfig>> mapCodec = GameMapConfig.codec();

        return instance.group(
                mapCodec.fieldOf("map").forGetter(BedWarsConfig::getMapConfig),
                CombatConfig.CODEC.fieldOf("combat").withDefault(CombatConfig.DEFAULT).forGetter(BedWarsConfig::getCombatConfig),
                GameModifier.CODEC.listOf().fieldOf("modifiers").withDefault(Collections.emptyList()).forGetter(BedWarsConfig::getModifiers),
                GameTeam.CODEC.listOf().fieldOf("teams").forGetter(BedWarsConfig::getTeams)
        ).apply(instance, BedWarsConfig::new);
    });

    private final GameMapConfig<BedWarsConfig> mapConfig;
    private final CombatConfig combatConfig;
    private final List<GameModifier> modifiers;
    private final List<GameTeam> teams;

    public BedWarsConfig(
            GameMapConfig<BedWarsConfig> mapConfig,
            CombatConfig combatConfig,
            List<GameModifier> modifiers,
            List<GameTeam> teams
    ) {
        this.mapConfig = mapConfig;
        this.combatConfig = combatConfig;
        this.modifiers = modifiers;
        this.teams = teams;
    }

    public GameMapConfig<BedWarsConfig> getMapConfig() {
        return this.mapConfig;
    }

    public CombatConfig getCombatConfig() {
        return this.combatConfig;
    }

    public List<GameModifier> getModifiers() {
        return this.modifiers;
    }

    public List<GameTeam> getTeams() {
        return this.teams;
    }
}
