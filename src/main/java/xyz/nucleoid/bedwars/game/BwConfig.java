package xyz.nucleoid.bedwars.game;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.bedwars.game.active.modifiers.GameModifier;
import xyz.nucleoid.bedwars.game.generator.BwSkyMapConfig;
import xyz.nucleoid.plasmid.game.config.CombatConfig;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.player.GameTeam;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class BwConfig {
    public static final Codec<BwConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.either(BwSkyMapConfig.CODEC, Identifier.CODEC).fieldOf("map").forGetter(config -> config.map),
                CombatConfig.CODEC.optionalFieldOf("combat", CombatConfig.DEFAULT).forGetter(config -> config.combat),
                GameModifier.CODEC.listOf().optionalFieldOf("modifiers", Collections.emptyList()).forGetter(config -> config.modifiers),
                GameTeam.CODEC.listOf().fieldOf("teams").forGetter(config -> config.teams),
                PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.players),
                Codec.BOOL.optionalFieldOf("keep_inventory", false).forGetter(config -> config.keepInventory)
        ).apply(instance, BwConfig::new);
    });

    public final Either<BwSkyMapConfig, Identifier> map;
    public final CombatConfig combat;
    public final List<GameModifier> modifiers;
    public final List<GameTeam> teams;
    public final PlayerConfig players;
    public final boolean keepInventory;

    public BwConfig(
            Either<BwSkyMapConfig, Identifier> map,
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
