package net.gegy1000.bedwars.game.bw;

import net.gegy1000.bedwars.game.GameTeam;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BwScoreboardLogic {
    private final BedWars game;

    private final Map<GameTeam, Team> scoreboardTeams = new HashMap<>();

    private ScoreboardObjective objective;
    private boolean dirty;

    BwScoreboardLogic(BedWars game) {
        this.game = game;
    }

    public void tick() {
        if (this.dirty) {
            this.rerender();
            this.dirty = false;
        }
    }

    public void setupScoreboard() {
        MinecraftServer server = this.game.world.getServer();
        ServerScoreboard scoreboard = server.getScoreboard();
        this.objective = new ScoreboardObjective(
                scoreboard, "bedwars",
                ScoreboardCriterion.DUMMY,
                new LiteralText("BedWars").formatted(Formatting.GOLD, Formatting.BOLD),
                ScoreboardCriterion.RenderType.INTEGER
        );
        scoreboard.addScoreboardObjective(this.objective);

        scoreboard.setObjectiveSlot(1, this.objective);
        this.rerender();
    }

    public void setupTeam(GameTeam team) {
        this.game.state.participantsFor(team).forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) return;

            this.setupPlayer(player, team);
        });
    }

    public void setupPlayer(ServerPlayerEntity player, GameTeam team) {
        MinecraftServer server = this.game.world.getServer();
        ServerScoreboard scoreboard = server.getScoreboard();
        scoreboard.addPlayerToTeam(player.getEntityName(), this.scoreboardTeam(team));
    }

    public void resetScoreboard() {
        MinecraftServer server = this.game.world.getServer();
        ServerScoreboard scoreboard = server.getScoreboard();
        this.scoreboardTeams.values().forEach(scoreboard::removeTeam);

        if (this.objective != null) {
            scoreboard.removeObjective(this.objective);
        }
    }

    public Team scoreboardTeam(GameTeam team) {
        return this.scoreboardTeams.computeIfAbsent(team, t -> {
            MinecraftServer server = this.game.world.getServer();
            ServerScoreboard scoreboard = server.getScoreboard();
            String teamKey = t.getName().asString();
            Team scoreboardTeam = scoreboard.getTeam(teamKey);
            if (scoreboardTeam == null) {
                scoreboardTeam = scoreboard.addTeam(teamKey);
                scoreboardTeam.setColor(team.getFormatting());
            }
            return scoreboardTeam;
        });
    }

    public void markDirty() {
        this.dirty = true;
    }

    private void rerender() {
        this.dirty = false;

        List<Text> lines = new ArrayList<>(10);

        long playersAlive = this.game.state.participants()
                .filter(participant -> !participant.eliminated && participant.inGame())
                .count();
        lines.add(new LiteralText(playersAlive + " players alive").formatted(Formatting.BLUE));
        lines.add(new LiteralText(""));

        lines.add(new LiteralText("Teams:").formatted(Formatting.BOLD));
        this.game.state.teams().forEach(teamState -> {
            long totalPlayerCount = this.game.state.participantsFor(teamState.team).count();
            long alivePlayerCount = this.game.state.participantsFor(teamState.team)
                    .filter(participant -> !participant.eliminated)
                    .count();

            String state = alivePlayerCount + "/" + totalPlayerCount;
            if (!teamState.hasBed) {
                state += " (no bed)";
            }

            Style teamNameStyle = new Style().setColor(teamState.team.getFormatting()).setBold(true);
            Style descriptionStyle = new Style().setColor(Formatting.GRAY).setBold(false);

            Text teamName = teamState.team.getName().deepCopy().setStyle(teamNameStyle);
            Text teamDescription = new LiteralText(": " + state).setStyle(descriptionStyle);
            lines.add(new LiteralText("  ").append(teamName).append(teamDescription));
        });

        this.render(lines.toArray(new Text[0]));
    }

    private void render(Text[] lines) {
        if (this.objective == null) {
            return;
        }

        MinecraftServer server = this.game.world.getServer();
        ServerScoreboard scoreboard = server.getScoreboard();

        render(scoreboard, this.objective, lines);
    }

    private static void render(ServerScoreboard scoreboard, ScoreboardObjective objective, Text[] lines) {
        clear(scoreboard, objective);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].asFormattedString();
            scoreboard.getPlayerScore(line, objective).setScore(lines.length - i);
        }
    }

    private static void clear(ServerScoreboard scoreboard, ScoreboardObjective objective) {
        Collection<ScoreboardPlayerScore> existing = scoreboard.getAllPlayerScores(objective);
        for (ScoreboardPlayerScore score : existing) {
            scoreboard.resetPlayerScore(score.getPlayerName(), objective);
        }
    }
}
