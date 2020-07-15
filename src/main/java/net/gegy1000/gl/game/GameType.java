package net.gegy1000.gl.game;

import com.mojang.serialization.Codec;
import net.gegy1000.gl.game.config.GameConfig;
import net.gegy1000.gl.registry.TinyRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public final class GameType<T extends Game, C extends GameConfig> {
    public static final TinyRegistry<GameType<?, ?>> REGISTRY = TinyRegistry.newStable();

    private final Identifier identifier;
    private final Open<T, C> open;
    private final Codec<C> configCodec;

    private GameType(Identifier identifier, Open<T, C> open, Codec<C> configCodec) {
        this.identifier = identifier;
        this.open = open;
        this.configCodec = configCodec;
    }

    public static <T extends Game, C extends GameConfig> GameType<T, C> register(
            Identifier identifier,
            Open<T, C> open,
            Codec<C> configCodec
    ) {
        GameType<T, C> type = new GameType<>(identifier, open, configCodec);
        REGISTRY.register(identifier, type);
        return type;
    }

    public CompletableFuture<T> open(MinecraftServer server, C config) {
        return this.open.open(server, config);
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

    public interface Open<T extends Game, C extends GameConfig> {
        CompletableFuture<T> open(MinecraftServer server, C config);
    }
}
