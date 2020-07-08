package net.gegy1000.bedwars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class GameType<T extends Game> {
    private static final Map<Identifier, GameType<?>> REGISTRY = new HashMap<>();

    private final Identifier identifier;

    private Text name;
    private Initializer<T> initializer;
    private int minPlayers = 1;
    private int maxPlayers = 100;

    private GameType(Identifier identifier) {
        this.identifier = identifier;
    }

    public static <T extends Game> GameType<T> register(Identifier identifier) {
        GameType<T> type = new GameType<>(identifier);
        REGISTRY.put(identifier, type);
        return type;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public CompletableFuture<T> initialize(ServerWorld world, BlockPos origin, List<ServerPlayerEntity> participants) {
        return this.initializer.initialize(world, origin, participants);
    }

    public GameType<T> setName(Text name) {
        this.name = name;
        return this;
    }

    public GameType<T> setInitializer(Initializer<T> initializer) {
        this.initializer = initializer;
        return this;
    }

    public GameType<T> setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
        return this;
    }

    public GameType<T> setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    public Text getName() {
        if (this.name == null) return new LiteralText(this.identifier.toString());
        return this.name;
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    @Nullable
    public static GameType get(Identifier identifier) {
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
            return ((GameType) obj).identifier.equals(this.identifier);
        }

        return false;
    }

    public interface Initializer<T extends Game> {
        CompletableFuture<T> initialize(ServerWorld world, BlockPos origin, List<ServerPlayerEntity> participants);
    }
}
