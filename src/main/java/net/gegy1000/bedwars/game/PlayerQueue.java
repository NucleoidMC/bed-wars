package net.gegy1000.bedwars.game;

import com.google.common.collect.Multimap;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.game.player.TeamAllocator;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlayerQueue {
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

    public Multimap<GameTeam, ServerPlayerEntity> allocatePlayers(List<GameTeam> teams) {
        TeamAllocator<GameTeam, ServerPlayerEntity> allocator = new TeamAllocator<>(teams);

        for (ServerPlayerEntity player : this.players) {
            GameTeam requestedTeam = this.requestedTeams.get(player);
            allocator.add(player, requestedTeam);
        }

        return allocator.build();
    }
}
