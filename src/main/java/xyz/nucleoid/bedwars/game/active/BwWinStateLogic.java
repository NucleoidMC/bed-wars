package xyz.nucleoid.bedwars.game.active;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BwWinStateLogic {
    private final BwActive game;

    BwWinStateLogic(BwActive game) {
        this.game = game;
    }

    @Nullable
    public WinResult checkWinResult() {
        this.checkEliminatedTeams();

        // if there's only one team, disable the win state
        if (this.game.getTeamCount() <= 1) {
            return null;
        }

        // If this is a development environment, disable the win state
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return null;
        }

        List<BwActive.TeamState> remainingTeams = this.game.teamsStates()
                .filter(team -> !team.eliminated)
                .collect(Collectors.toList());

        if (remainingTeams.size() <= 1) {
            if (remainingTeams.size() == 1) {
                BwActive.TeamState winningTeam = remainingTeams.get(0);
                return WinResult.team(winningTeam.team);
            } else {
                return WinResult.draw();
            }
        }

        return null;
    }

    private void checkEliminatedTeams() {
        Stream<BwActive.TeamState> eliminatedTeams = this.game.teamsStates()
                .filter(team -> !team.eliminated)
                .filter(state -> {
                    long remainingCount = this.countRemainingPlayers(state.team.key());
                    return remainingCount <= 0;
                });

        eliminatedTeams.forEach(this::eliminateTeam);
    }

    public void eliminatePlayer(BwParticipant participant) {
        participant.eliminated = true;

        BwActive.TeamState teamState = this.game.teamState(participant.team.key());
        if (teamState != null && !teamState.eliminated) {
            long remainingCount = this.countRemainingPlayers(participant.team.key());
            if (remainingCount <= 0) {
                this.eliminateTeam(teamState);
            }
        }
    }

    private long countRemainingPlayers(GameTeamKey team) {
        return this.game.participantsFor(team)
                .filter(BwParticipant::isAlive)
                .count();
    }

    private void eliminateTeam(BwActive.TeamState teamState) {
        teamState.eliminated = true;

        this.game.participantsFor(teamState.team.key()).forEach(participant -> {
            participant.eliminated = true;
        });

        this.game.broadcast.broadcastTeamEliminated(teamState.team);
    }

    public record WinResult(@Nullable GameTeam team) {
        public static WinResult team(GameTeam team) {
            return new WinResult(team);
        }

        public static WinResult draw() {
            return new WinResult(null);
        }

        public boolean isDraw() {
            return this.team == null;
        }
    }
}
