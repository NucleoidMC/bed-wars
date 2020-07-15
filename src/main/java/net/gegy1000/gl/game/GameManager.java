package net.gegy1000.gl.game;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GameManager {
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("game-manager")
                    .setDaemon(true)
                    .build()
    );

    private static State state = Inactive.INSTANCE;

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T extends Game> T openFor(GameType<T, ?> gameType) {
        Optional<Open<?>> activeOpt = open();
        if (activeOpt.isPresent()) {
            Open<?> open = activeOpt.get();
            GameAndConfig<?> game = open.game;
            if (game.getConfigured().getType().equals(gameType)) {
                return (T) game.getGame();
            }
        }
        return null;
    }

    public static Optional<Inactive> inactive() {
        fixState();
        if (state instanceof Inactive) {
            return Optional.of((Inactive) state);
        }
        return Optional.empty();
    }

    public static Optional<Open<?>> open() {
        fixState();
        if (state instanceof Open<?>) {
            return Optional.of((Open<?>) state);
        }
        return Optional.empty();
    }

    private static void fixState() {
        if (state instanceof Open) {
            Open<?> open = (Open<?>) GameManager.state;
            if (open.game.getGame().isClosed()) {
                state = Inactive.INSTANCE;
            }
        }
    }

    public interface State {
    }

    public static class Inactive implements State {
        public static final Inactive INSTANCE = new Inactive();

        private Inactive() {
        }

        public <T extends Game> CompletableFuture<GameAndConfig<T>> open(
                MinecraftServer server,
                ConfiguredGame<T, ?> configuredGame
        ) {
            state = new Starting();

            return configuredGame.open(server).thenApplyAsync(game -> {
                GameAndConfig<T> gameAndConfig = new GameAndConfig<>(game, configuredGame);
                state = new Open<>(gameAndConfig);
                return gameAndConfig;
            }, server);
        }
    }

    public static class Starting implements State {
    }

    public static class Open<T extends Game> implements State {
        final GameAndConfig<T> game;

        Open(GameAndConfig<T> game) {
            this.game = game;
        }

        public String getName() {
            return this.game.getConfigured().getName();
        }

        public StartResult requestStart() {
            return this.game.getGame().requestStart();
        }

        public JoinResult offerPlayer(ServerPlayerEntity player) {
            return this.game.getGame().offerPlayer(player);
        }

        public CompletableFuture<GameAndConfig<T>> stop() {
            state = Inactive.INSTANCE;
            return this.game.getGame().stop().thenApply(v -> this.game);
        }
    }
}
