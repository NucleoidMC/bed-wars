package net.gegy1000.bedwars.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.game.config.CombatConfig;
import net.gegy1000.gl.game.config.GameConfig;
import net.gegy1000.gl.game.config.GameMapConfig;
import net.gegy1000.gl.game.config.PlayerConfig;
import net.gegy1000.gl.game.modifier.GameModifier;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class BedWarsConfig implements GameConfig {
    public static final Codec<BedWarsConfig> CODEC = RecordCodecBuilder.create(instance -> {
        Codec<GameMapConfig<BedWarsConfig>> mapCodec = GameMapConfig.codec();

        return instance.group(
                mapCodec.fieldOf("map").forGetter(BedWarsConfig::getMapConfig),
                CombatConfig.CODEC.fieldOf("combat").withDefault(CombatConfig.DEFAULT).forGetter(BedWarsConfig::getCombatConfig),
                GameModifier.CODEC.listOf().fieldOf("modifiers").withDefault(Collections.emptyList()).forGetter(BedWarsConfig::getModifiers),
                GameTeam.CODEC.listOf().fieldOf("teams").forGetter(BedWarsConfig::getTeams),
                PlayerConfig.CODEC.fieldOf("players").forGetter(BedWarsConfig::getPlayerConfig)
        ).apply(instance, BedWarsConfig::new);
    });

    private final GameMapConfig<BedWarsConfig> mapConfig;
    private final CombatConfig combatConfig;
    private final List<GameModifier> modifiers;
    private final List<GameTeam> teams;
    private final PlayerConfig playerConfig;

    public BedWarsConfig(
            GameMapConfig<BedWarsConfig> mapConfig,
            CombatConfig combatConfig,
            List<GameModifier> modifiers,
            List<GameTeam> teams,
            PlayerConfig playerConfig
    ) {
        this.mapConfig = mapConfig;
        this.combatConfig = combatConfig;
        this.modifiers = modifiers;
        this.teams = teams;
        this.playerConfig = playerConfig;
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

    public PlayerConfig getPlayerConfig() {
        return this.playerConfig;
    }

    @Nullable
    public GameTeam getTeam(String key) {
        for (GameTeam team : this.teams) {
            if (team.getKey().equals(key)) {
                return team;
            }
        }
        return null;
    }
}
