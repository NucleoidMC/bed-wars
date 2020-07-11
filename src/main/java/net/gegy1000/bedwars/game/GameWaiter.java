package net.gegy1000.bedwars.game;

import net.gegy1000.bedwars.game.config.PlayerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class GameWaiter<T extends Game> {
    private final ConfiguredGame<T, ?> game;
    private final Set<ServerPlayerEntity> participants = new HashSet<>();

    public GameWaiter(ConfiguredGame<T, ?> game) {
        this.game = game;
    }

    public ConfiguredGame<T, ?> getGame() {
        return this.game;
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        return this.participants.remove(player);
    }

    public JoinResult joinPlayer(ServerPlayerEntity player) {
        if (this.participants.size() >= this.game.getPlayerConfig().getMaxPlayers()) {
            return JoinResult.GAME_FULL;
        }

        if (!this.participants.add(player)) {
            return JoinResult.ALREADY_JOINED;
        }

        return JoinResult.OK;
    }

    public boolean canStart() {
        PlayerConfig playerConfig = this.game.getPlayerConfig();
        return this.participants.size() >= playerConfig.getMinPlayers()
                && this.participants.size() <= playerConfig.getMaxPlayers();
    }

    public CompletableFuture<GameAndConfig<T>> start(MinecraftServer server) {
        List<ServerPlayerEntity> participants = new ArrayList<>(this.participants);
        Collections.shuffle(participants);

        return this.game.initialize(server, participants)
                .thenApply(game -> new GameAndConfig<>(game, this.game));
    }
}
