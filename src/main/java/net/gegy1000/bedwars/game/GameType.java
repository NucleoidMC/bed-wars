package net.gegy1000.bedwars.game;

import com.mojang.serialization.Codec;
import net.gegy1000.bedwars.game.config.GameConfig;
import net.gegy1000.bedwars.util.TinyRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GameType<T extends Game, C extends GameConfig> {
    public static final TinyRegistry<GameType<?, ?>> REGISTRY = TinyRegistry.newStable();

    private final Identifier identifier;
    private final Initializer<T, C> initializer;
    private final Codec<C> configCodec;

    private GameType(Identifier identifier, Initializer<T, C> initializer, Codec<C> configCodec) {
        this.identifier = identifier;
        this.initializer = initializer;
        this.configCodec = configCodec;
    }

    public static <T extends Game, C extends GameConfig> GameType<T, C> register(
            Identifier identifier,
            Initializer<T, C> initializer,
            Codec<C> configCodec
    ) {
        GameType<T, C> type = new GameType<>(identifier, initializer, configCodec);
        REGISTRY.register(identifier, type);
        return type;
    }

    public CompletableFuture<T> initialize(MinecraftServer server, List<ServerPlayerEntity> participants, C config) {
        return this.initializer.initialize(server, participants, config);
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public Codec<C> getConfigCodec() {
        return this.configCodec;
    }

    @Nullable
    public static GameType<?, ?> get(Identifier identifier) {
        return REGISTRY.get(identifier);
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof GameType) {
            return ((GameType<?, ?>) obj).identifier.equals(this.identifier);
        }

        return false;
    }

    public interface Initializer<T extends Game, C extends GameConfig> {
        CompletableFuture<T> initialize(MinecraftServer server, List<ServerPlayerEntity> participants, C config);
    }
}
