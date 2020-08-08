package net.gegy1000.bedwars.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.bedwars.game.active.modifiers.GameModifier;
import net.gegy1000.bedwars.game.generator.BwSkyMapConfig;
import net.gegy1000.plasmid.game.config.CombatConfig;
import net.gegy1000.plasmid.game.config.GameConfig;
import net.gegy1000.plasmid.game.config.PlayerConfig;
import net.gegy1000.plasmid.game.player.GameTeam;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class BwConfig implements GameConfig {
    public static final Codec<BwConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                BwSkyMapConfig.CODEC.fieldOf("map").forGetter(config -> config.map),
                CombatConfig.CODEC.optionalFieldOf("combat", CombatConfig.DEFAULT).forGetter(config -> config.combat),
                GameModifier.CODEC.listOf().optionalFieldOf("modifiers", Collections.emptyList()).forGetter(config -> config.modifiers),
                GameTeam.CODEC.listOf().fieldOf("teams").forGetter(config -> config.teams),
                PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.players),
                Codec.BOOL.optionalFieldOf("keep_inventory", false).forGetter(config -> config.keepInventory)
        ).apply(instance, BwConfig::new);
    });

    public final BwSkyMapConfig map;
    public final CombatConfig combat;
    public final List<GameModifier> modifiers;
    public final List<GameTeam> teams;
    public final PlayerConfig players;
    public final boolean keepInventory;

    public BwConfig(
            BwSkyMapConfig map,
            CombatConfig combat,
            List<GameModifier> modifiers,
            List<GameTeam> teams,
            PlayerConfig players,
            boolean keepInventory
    ) {
        this.map = map;
        this.combat = combat;
        this.modifiers = modifiers;
        this.teams = teams;
        this.players = players;
        this.keepInventory = keepInventory;
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
