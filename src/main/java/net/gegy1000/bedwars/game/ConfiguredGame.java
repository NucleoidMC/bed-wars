package net.gegy1000.bedwars.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.gegy1000.bedwars.game.config.GameConfig;
import net.gegy1000.bedwars.game.config.PlayerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ConfiguredGame<T extends Game, C extends GameConfig> {
    public static final Codec<ConfiguredGame<?, ?>> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("name").forGetter(ConfiguredGame::getName),
                TypeAndConfig.CODEC.fieldOf("game").forGetter(game -> new TypeAndConfig(game.type, game.config)),
                PlayerConfig.CODEC.fieldOf("players").forGetter(ConfiguredGame::getPlayerConfig)
        ).apply(instance, ConfiguredGame::createUnchecked);
    });

    private final String name;
    private final GameType<T, C> type;
    private final C config;
    private final PlayerConfig playerConfig;

    private ConfiguredGame(String name, GameType<T, C> type, C config, PlayerConfig playerConfig) {
        this.name = name;
        this.type = type;
        this.config = config;
        this.playerConfig = playerConfig;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Game, C extends GameConfig> ConfiguredGame<T, C> createUnchecked(
            String name,
            TypeAndConfig typeAndConfig,
            PlayerConfig playerConfig
    ) {
        GameType<T, C> type = (GameType<T, C>) typeAndConfig.type;
        C config = (C) typeAndConfig.config;
        return new ConfiguredGame<>(name, type, config, playerConfig);
    }

    public CompletableFuture<T> initialize(MinecraftServer server, List<ServerPlayerEntity> participants) {
        return this.type.initialize(server, participants, this.config);
    }

    public String getName() {
        return this.name;
    }

    public GameType<T, C> getType() {
        return this.type;
    }

    public C getConfig() {
        return this.config;
    }

    public PlayerConfig getPlayerConfig() {
        return this.playerConfig;
    }

    private static class TypeAndConfig {
        static final Codec<TypeAndConfig> CODEC = GameType.REGISTRY.dispatchStable(
                o -> o.type,
                type -> type.getConfigCodec().xmap(
                        config -> new TypeAndConfig(type, config),
                        tac -> coerceConfigUnchecked(tac.config)
                )
        );

        final GameType<?, ?> type;
        final GameConfig config;

        TypeAndConfig(GameType<?, ?> type, GameConfig config) {
            this.type = type;
            this.config = config;
        }

        @SuppressWarnings("unchecked")
        private static <T extends GameConfig> T coerceConfigUnchecked(GameConfig config) {
            return (T) config;
        }
    }
}
