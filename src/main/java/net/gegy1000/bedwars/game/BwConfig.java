package net.gegy1000.bedwars.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.plasmid.game.GameTeam;
import net.gegy1000.plasmid.game.config.CombatConfig;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.config.GameMapConfig;
import net.gegy1000.plasmid.game.config.PlayerConfig;
import net.gegy1000.bedwars.game.active.modifiers.GameModifier;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class BwConfig implements GameConfig {
    public static final Codec<BwConfig> CODEC = RecordCodecBuilder.create(instance -> {
        Codec<GameMapConfig<BwConfig>> mapCodec = GameMapConfig.codec();

        return instance.group(
                mapCodec.fieldOf("map").forGetter(BwConfig::getMapConfig),
                CombatConfig.CODEC.fieldOf("combat").withDefault(CombatConfig.DEFAULT).forGetter(BwConfig::getCombatConfig),
                GameModifier.CODEC.listOf().fieldOf("modifiers").withDefault(Collections.emptyList()).forGetter(BwConfig::getModifiers),
                GameTeam.CODEC.listOf().fieldOf("teams").forGetter(BwConfig::getTeams),
                PlayerConfig.CODEC.fieldOf("players").forGetter(BwConfig::getPlayerConfig),
                Codec.BOOL.optionalFieldOf("keep_inventory", false).forGetter(BwConfig::shouldKeepInventory)
        ).apply(instance, BwConfig::new);
    });

    private final GameMapConfig<BwConfig> mapConfig;
    private final CombatConfig combatConfig;
    private final List<GameModifier> modifiers;
    private final List<GameTeam> teams;
    private final PlayerConfig playerConfig;
    private final boolean keepInventory;

    public BwConfig(
            GameMapConfig<BwConfig> mapConfig,
            CombatConfig combatConfig,
            List<GameModifier> modifiers,
            List<GameTeam> teams,
            PlayerConfig playerConfig,
            boolean keepInventory
    ) {
        this.mapConfig = mapConfig;
        this.combatConfig = combatConfig;
        this.modifiers = modifiers;
        this.teams = teams;
        this.playerConfig = playerConfig;
        this.keepInventory = keepInventory;
    }

    public GameMapConfig<BwConfig> getMapConfig() {
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

    public boolean shouldKeepInventory() {
        return this.keepInventory;
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
