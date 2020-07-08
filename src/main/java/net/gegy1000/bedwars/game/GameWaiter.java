package net.gegy1000.bedwars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class GameWaiter<T extends Game> {
    private final GameType<T> gameType;
    private final Set<ServerPlayerEntity> participants = new HashSet<>();

    public GameWaiter(GameType<T> gameType) {
        this.gameType = gameType;
    }

    public GameType<T> getGameType() {
        return this.gameType;
    }

    public boolean removePlayer(ServerPlayerEntity player) {
        return this.participants.remove(player);
    }

    public boolean joinPlayer(ServerPlayerEntity player) {
        if (this.participants.size() >= this.gameType.getMaxPlayers()) {
            return false;
        }
        return this.participants.add(player);
    }

    public boolean canStart() {
        return this.participants.size() >= this.gameType.getMinPlayers()
                && this.participants.size() <= this.gameType.getMaxPlayers();
    }

    public CompletableFuture<T> start(ServerWorld world, BlockPos origin) {
        List<ServerPlayerEntity> participants = new ArrayList<>(this.participants);
        Collections.shuffle(participants);

        return this.gameType.initialize(world, origin, participants);
    }
}
