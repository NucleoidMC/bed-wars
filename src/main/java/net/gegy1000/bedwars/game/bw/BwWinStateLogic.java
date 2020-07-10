package net.gegy1000.bedwars.game.bw;

import net.gegy1000.bedwars.game.GameTeam;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BwWinStateLogic {
    private final BedWars game;

    BwWinStateLogic(BedWars game) {
        this.game = game;
    }

    @Nullable
    public WinResult checkWinResult() {
        this.checkEliminatedTeams();

        // if there's only one team, disable the win state
        if (this.game.state.getTeamCount() <= 1) {
            return null;
        }

        List<BwState.TeamState> remainingTeams = this.game.state.teams()
                .filter(team -> !team.eliminated)
                .collect(Collectors.toList());

        if (remainingTeams.size() <= 1) {
            if (remainingTeams.size() == 1) {
                BwState.TeamState winningTeam = remainingTeams.get(0);
                return WinResult.team(winningTeam.team);
            } else {
                return WinResult.draw();
            }
        }

        return null;
    }

    private void checkEliminatedTeams() {
        Stream<BwState.TeamState> eliminatedTeams = this.game.state.teams()
                .filter(team -> !team.eliminated)
                .filter(team -> {
                    long remainingCount = this.countRemainingPlayers(team.team);
                    return remainingCount <= 0;
                });

        eliminatedTeams.forEach(this::eliminateTeam);
    }

    public void eliminatePlayer(BwState.Participant participant) {
        participant.eliminated = true;

        BwState.TeamState teamState = this.game.state.getTeam(participant.team);
        if (teamState != null && !teamState.eliminated) {
            long remainingCount = this.countRemainingPlayers(participant.team);
            if (remainingCount <= 0) {
                this.eliminateTeam(teamState);
            }
        }

        this.game.scoreboardLogic.markDirty();
    }

    private long countRemainingPlayers(GameTeam team) {
        return this.game.state.participantsFor(team)
                .filter(p -> !p.eliminated)
                .filter(BwState.Participant::inGame)
                .count();
    }

    private void eliminateTeam(BwState.TeamState teamState) {
        teamState.eliminated = true;

        this.game.state.participantsFor(teamState.team).forEach(participant -> {
            participant.eliminated = true;
        });

        this.game.broadcast.broadcastTeamEliminated(teamState.team);
        this.game.scoreboardLogic.markDirty();
    }

    public static class WinResult {
        private final GameTeam team;

        private WinResult(GameTeam team) {
            this.team = team;
        }

        public static WinResult team(GameTeam team) {
            return new WinResult(team);
        }

        public static WinResult draw() {
            return new WinResult(null);
        }

        @Nullable
        public GameTeam getTeam() {
            return this.team;
        }

        public boolean isDraw() {
            return this.team == null;
        }
    }
}
