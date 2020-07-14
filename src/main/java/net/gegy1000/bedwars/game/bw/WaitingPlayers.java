package net.gegy1000.bedwars.game.bw;

import net.gegy1000.bedwars.game.GameTeam;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WaitingPlayers {
    private final Set<ServerPlayerEntity> players = new HashSet<>();
    private final Map<ServerPlayerEntity, GameTeam> requestedTeams = new HashMap<>();

    boolean addPlayer(ServerPlayerEntity player) {
        return this.players.add(player);
    }

    void requestTeam(ServerPlayerEntity player, GameTeam team) {
        if (this.players.contains(player)) {
            this.requestedTeams.put(player, team);
        }
    }

    public boolean contains(ServerPlayerEntity player) {
        return this.players.contains(player);
    }

    public int size() {
        return this.players.size();
    }

    public List<ServerPlayerEntity> takePlayers() {
        List<ServerPlayerEntity> players = new ArrayList<>(this.players);
        Collections.shuffle(players);
        return players;
    }

    @Nullable
    public GameTeam getRequestedTeam(ServerPlayerEntity player) {
        return this.requestedTeams.get(player);
    }
}
