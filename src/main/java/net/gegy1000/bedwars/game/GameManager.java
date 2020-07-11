package net.gegy1000.bedwars.game;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
    public static <T extends Game> T activeFor(GameType<T, ?> gameType) {
        Optional<Active<?>> activeOpt = active();
        if (activeOpt.isPresent()) {
            Active<?> active = activeOpt.get();
            GameAndConfig<?> game = active.game;
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

    public static Optional<Recruiting<?>> recruiting() {
        fixState();
        if (state instanceof Recruiting<?>) {
            return Optional.of((Recruiting<?>) state);
        }
        return Optional.empty();
    }

    public static Optional<Active<?>> active() {
        fixState();
        if (state instanceof Active<?>) {
            return Optional.of((Active<?>) state);
        }
        return Optional.empty();
    }

    private static void fixState() {
        if (state instanceof Active) {
            Active<?> active = (Active<?>) GameManager.state;
            if (!active.game.getGame().isActive()) {
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

        public void recruit(ConfiguredGame<?, ?> gameType) {
            state = new Recruiting<>(new GameWaiter<>(gameType));
        }
    }

    public static class Recruiting<T extends Game> implements State {
        final GameWaiter<T> waiter;

        Recruiting(GameWaiter<T> waiter) {
            this.waiter = waiter;
        }

        public ConfiguredGame<T, ?> getGame() {
            return this.waiter.getGame();
        }

        public JoinResult joinPlayer(ServerPlayerEntity player) {
            return this.waiter.joinPlayer(player);
        }

        public boolean canStart() {
            return this.waiter.canStart();
        }

        public CompletableFuture<GameAndConfig<T>> start(MinecraftServer server) {
            state = new Starting();
            return this.waiter.start(server)
                    .handle((game, throwable) -> {
                        if (throwable != null) {
                            state = Inactive.INSTANCE;
                            throw new CompletionException(throwable);
                        } else {
                            state = new Active<>(game);
                        }
                        return game;
                    });
        }
    }

    public static class Starting implements State {
    }

    public static class Active<T extends Game> implements State {
        final GameAndConfig<T> game;

        Active(GameAndConfig<T> game) {
            this.game = game;
        }

        public CompletableFuture<GameAndConfig<T>> stop() {
            state = Inactive.INSTANCE;
            return this.game.getGame().stop().thenApply(v -> this.game);
        }
    }
}
