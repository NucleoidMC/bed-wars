package net.gegy1000.bedwars.game.bw;

import com.mojang.datafixers.util.Either;
import net.gegy1000.bedwars.game.JoinResult;
import net.gegy1000.bedwars.game.StartResult;
import net.gegy1000.bedwars.game.config.PlayerConfig;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BwWaitingLogic {
    private final BedWars game;
    private final Set<ServerPlayerEntity> players = new HashSet<>();

    public BwWaitingLogic(BedWars game) {
        this.game = game;
    }

    public JoinResult offerPlayer(ServerPlayerEntity player) {
        PlayerConfig playerConfig = this.game.config.getPlayerConfig();

        if (this.players.size() >= playerConfig.getMaxPlayers()) {
            return JoinResult.GAME_FULL;
        }

        if (this.players.add(player)) {
            this.game.takeSnapshot(player);
            this.game.playerLogic.resetPlayer(player);
            this.game.playerLogic.spawnSpectator(player);

            return JoinResult.OK;
        } else {
            return JoinResult.ALREADY_JOINED;
        }
    }

    public Either<BwState, StartResult> tryStart() {
        PlayerConfig playerConfig = this.game.config.getPlayerConfig();
        if (this.players.size() < playerConfig.getMinPlayers()) {
            return Either.right(StartResult.NOT_ENOUGH_PLAYERS);
        }

        List<ServerPlayerEntity> players = new ArrayList<>(this.players);
        Collections.shuffle(players);

        BwState state = BwState.start(players, this.game.config);
        return Either.left(state);
    }
}
