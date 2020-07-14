package net.gegy1000.bedwars.game.bw;

import net.gegy1000.bedwars.game.GameTeam;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BwState {
    public static final long RESPAWN_TICKS = 20 * 5;

    private final Map<UUID, Participant> participants = new HashMap<>();
    private final Map<GameTeam, TeamState> teams = new HashMap<>();

    private BwState() {
    }

    public static BwState start(List<ServerPlayerEntity> players, BedWarsConfig config) {
        BwState state = new BwState();

        state.allocatePlayers(players, config);

        for (GameTeam team : config.getTeams()) {
            List<Participant> participants = state.participantsFor(team)
                    .collect(Collectors.toList());

            if (!participants.isEmpty()) {
                TeamState teamState = new TeamState(team);
                participants.forEach(participant -> {
                    teamState.players.add(participant.playerId);
                });

                state.teams.put(team, teamState);
            }
        }

        return state;
    }

    private void allocatePlayers(List<ServerPlayerEntity> players, BedWarsConfig config) {
        List<GameTeam> teams = new ArrayList<>(config.getTeams());

        Collections.shuffle(teams);
        Collections.shuffle(players);

        int teamIndex = 0;
        for (ServerPlayerEntity player : players) {
            GameTeam team = teams.get(teamIndex++ % teams.size());
            this.participants.put(player.getUuid(), new Participant(player, team));
        }
    }

    @Nullable
    public Participant getParticipant(PlayerEntity player) {
        return this.participants.get(player.getUuid());
    }

    @Nullable
    public GameTeam getTeam(UUID id) {
        Participant participant = this.participants.get(id);
        if (participant != null) {
            return participant.team;
        }
        return null;
    }

    @Nullable
    public Participant getParticipant(UUID id) {
        return this.participants.get(id);
    }

    public boolean isParticipant(PlayerEntity player) {
        return this.participants.containsKey(player.getUuid());
    }

    public Stream<Participant> participantsFor(GameTeam team) {
        return this.participants.values().stream()
                .filter(participant -> participant.team == team);
    }

    public Stream<Participant> participants() {
        return this.participants.values().stream();
    }

    public Stream<ServerPlayerEntity> players() {
        return this.participants().map(Participant::player).filter(Objects::nonNull);
    }

    public Stream<TeamState> teams() {
        return this.teams.values().stream();
    }

    public int getTeamCount() {
        return this.teams.size();
    }

    @Nullable
    public TeamState getTeam(GameTeam team) {
        return this.teams.get(team);
    }

    public static class Participant {
        private final ServerWorld world;
        public final UUID playerId;
        public final GameTeam team;

        public ArmorLevel armorLevel = ArmorLevel.LEATHER;

        BwMap.TeamSpawn respawningAt;
        long respawnTime = -1;
        boolean eliminated;

        Participant(ServerPlayerEntity player, GameTeam team) {
            this.world = player.getServerWorld();
            this.playerId = player.getUuid();
            this.team = team;
        }

        public void startRespawning(BwMap.TeamSpawn spawn) {
            this.respawnTime = this.world.getTime() + RESPAWN_TICKS;
            this.respawningAt = spawn;
        }

        public void stopRespawning() {
            this.respawningAt = null;
            this.respawnTime = -1;
        }

        public boolean isRespawning() {
            return this.respawningAt != null;
        }

        @Nullable
        public ServerPlayerEntity player() {
            return this.world.getServer().getPlayerManager().getPlayer(this.playerId);
        }

        public boolean inGame() {
            return this.player() != null;
        }
    }

    public static class TeamState {
        public static final int MAX_SHARPNESS = 3;
        public static final int MAX_PROTECTION = 3;

        final Set<UUID> players = new HashSet<>();
        final GameTeam team;
        boolean hasBed = true;
        boolean eliminated;

        public boolean trapSet;
        public boolean healPool;
        public boolean hasteEnabled;
        public int swordSharpness;
        public int armorProtection;

        TeamState(GameTeam team) {
            this.team = team;
        }
    }
}
