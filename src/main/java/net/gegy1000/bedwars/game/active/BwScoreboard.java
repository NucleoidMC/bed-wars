package net.gegy1000.bedwars.game.active;

import net.gegy1000.plasmid.game.player.GameTeam;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BwScoreboard implements AutoCloseable {
    private final BwActive game;

    private final Map<GameTeam, Team> scoreboardTeams = new HashMap<>();
    private final ScoreboardObjective objective;

    private boolean dirty = true;

    private long ticks;

    private BwScoreboard(BwActive game, ScoreboardObjective objective) {
        this.game = game;
        this.objective = objective;
    }

    public static BwScoreboard create(BwActive game) {
        MinecraftServer server = game.world.getServer();
        ServerScoreboard scoreboard = server.getScoreboard();

        ScoreboardObjective objective = new ScoreboardObjective(
                scoreboard, "bedwars",
                ScoreboardCriterion.DUMMY,
                new LiteralText("BedWars").formatted(Formatting.GOLD, Formatting.BOLD),
                ScoreboardCriterion.RenderType.INTEGER
        );
        scoreboard.addScoreboardObjective(objective);

        scoreboard.setObjectiveSlot(1, objective);

        return new BwScoreboard(game, objective);
    }

    public void tick() {
        this.ticks++;

        if (this.dirty || this.ticks % 20 == 0) {
            this.rerender();
            this.dirty = false;
        }
    }

    public void addTeam(GameTeam team) {
        this.game.participantsFor(team).forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player == null) return;

            this.addPlayer(player, team);
        });
    }

    private void addPlayer(ServerPlayerEntity player, GameTeam team) {
        MinecraftServer server = this.game.world.getServer();

        ServerScoreboard scoreboard = server.getScoreboard();
        scoreboard.addPlayerToTeam(player.getEntityName(), this.scoreboardTeam(team));
    }

    public Team scoreboardTeam(GameTeam team) {
        return this.scoreboardTeams.computeIfAbsent(team, t -> {
            MinecraftServer server = this.game.world.getServer();
            ServerScoreboard scoreboard = server.getScoreboard();
            String teamKey = t.getDisplay();
            Team scoreboardTeam = scoreboard.getTeam(teamKey);
            if (scoreboardTeam == null) {
                scoreboardTeam = scoreboard.addTeam(teamKey);
                scoreboardTeam.setColor(team.getFormatting());
                scoreboardTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
            }
            return scoreboardTeam;
        });
    }

    public void markDirty() {
        this.dirty = true;
    }

    private void rerender() {
        List<String> lines = new ArrayList<>(10);

        long seconds = (this.ticks / 20) % 60;
        long minutes = this.ticks / (20 * 60);

        lines.add(String.format("%sTime: %s%02d:%02d", Formatting.RED.toString() + Formatting.BOLD, Formatting.RESET, minutes, seconds));

        long playersAlive = this.game.participants()
                .filter(BwParticipant::isAlive)
                .count();
        lines.add(Formatting.BLUE.toString() + playersAlive + " players alive");
        lines.add("");

        lines.add(Formatting.BOLD + "Teams:");
        this.game.teams().forEach(teamState -> {
            long totalPlayerCount = this.game.participantsFor(teamState.team).count();
            long alivePlayerCount = this.game.participantsFor(teamState.team)
                    .filter(BwParticipant::isAlive)
                    .count();

            if (!teamState.eliminated) {
                String state = alivePlayerCount + "/" + totalPlayerCount;
                if (!teamState.hasBed) {
                    state += " (no bed)";
                }

                String nameFormat = teamState.team.getFormatting().toString() + Formatting.BOLD.toString();
                String descriptionFormat = Formatting.RESET.toString() + Formatting.GRAY.toString();

                String name = teamState.team.getDisplay();
                lines.add("  " + nameFormat + name + ": " + descriptionFormat + state);
            } else {
                String nameFormat = teamState.team.getFormatting().toString() + Formatting.BOLD.toString() + Formatting.STRIKETHROUGH.toString();
                String descriptionFormat = Formatting.RESET.toString() + Formatting.RED.toString();

                String name = teamState.team.getDisplay();
                lines.add("  " + nameFormat + name + descriptionFormat + ": eliminated!");
            }
        });

        this.render(lines.toArray(new String[0]));
    }

    private void render(String[] lines) {
        MinecraftServer server = this.game.world.getServer();
        ServerScoreboard scoreboard = server.getScoreboard();

        render(scoreboard, this.objective, lines);
    }

    private static void render(ServerScoreboard scoreboard, ScoreboardObjective objective, String[] lines) {
        clear(scoreboard, objective);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            scoreboard.getPlayerScore(line, objective).setScore(lines.length - i);
        }
    }

    private static void clear(ServerScoreboard scoreboard, ScoreboardObjective objective) {
        Collection<ScoreboardPlayerScore> existing = scoreboard.getAllPlayerScores(objective);
        for (ScoreboardPlayerScore score : existing) {
            scoreboard.resetPlayerScore(score.getPlayerName(), objective);
        }
    }

    @Override
    public void close() {
        MinecraftServer server = this.game.world.getServer();

        ServerScoreboard scoreboard = server.getScoreboard();
        this.scoreboardTeams.values().forEach(scoreboard::removeTeam);

        scoreboard.removeObjective(this.objective);
    }
}
