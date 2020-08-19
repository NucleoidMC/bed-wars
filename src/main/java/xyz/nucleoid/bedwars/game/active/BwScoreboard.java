package xyz.nucleoid.bedwars.game.active;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.widget.SidebarWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BwScoreboard implements AutoCloseable {
    private final BwActive game;

    private final Map<GameTeam, Team> scoreboardTeams = new HashMap<>();
    private final SidebarWidget sidebar;

    private long ticks;

    private BwScoreboard(BwActive game, SidebarWidget sidebar) {
        this.game = game;
        this.sidebar = sidebar;
    }

    public static BwScoreboard create(BwActive game) {
        Text title = new LiteralText("BedWars").formatted(Formatting.GOLD, Formatting.BOLD);
        SidebarWidget sidebar = SidebarWidget.open(title, game.gameWorld.getPlayerSet());
        return new BwScoreboard(game, sidebar);
    }

    public void tick() {
        if (this.ticks++ % 20 == 0) {
            this.render();
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

    private void render() {
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

        this.sidebar.set(lines.toArray(new String[0]));
    }

    @Override
    public void close() {
        MinecraftServer server = this.game.world.getServer();

        ServerScoreboard scoreboard = server.getScoreboard();
        this.scoreboardTeams.values().forEach(scoreboard::removeTeam);

        this.sidebar.close();
    }
}
