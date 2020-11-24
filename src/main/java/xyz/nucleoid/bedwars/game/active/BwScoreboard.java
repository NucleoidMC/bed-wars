package xyz.nucleoid.bedwars.game.active;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;
import xyz.nucleoid.plasmid.widget.SidebarWidget;

import java.util.HashMap;
import java.util.Map;

public final class BwScoreboard implements AutoCloseable {
    private final BwActive game;

    private final Map<GameTeam, Team> scoreboardTeams = new HashMap<>();
    private final SidebarWidget sidebar;

    private long ticks;

    BwScoreboard(BwActive game, SidebarWidget sidebar) {
        this.game = game;
        this.sidebar = sidebar;
    }

    public static BwScoreboard create(BwActive game, GlobalWidgets widgets) {
        Text title = new LiteralText("BedWars").formatted(Formatting.GOLD, Formatting.BOLD);
        SidebarWidget sidebar = widgets.addSidebar(title);
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
            String teamKey = generateTeamKey();
            Team scoreboardTeam = scoreboard.getTeam(teamKey);
            if (scoreboardTeam == null) {
                scoreboardTeam = scoreboard.addTeam(teamKey);
                scoreboardTeam.setDisplayName(new LiteralText(t.getDisplay()));
                scoreboardTeam.setColor(team.getFormatting());
                scoreboardTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
                scoreboardTeam.setFriendlyFireAllowed(false);
            }
            return scoreboardTeam;
        });
    }

    private void render() {
        this.sidebar.set(content -> {
            long seconds = (this.ticks / 20) % 60;
            long minutes = this.ticks / (20 * 60);

            content.writeLine(String.format("%sTime: %s%02d:%02d", Formatting.RED.toString() + Formatting.BOLD, Formatting.RESET, minutes, seconds));

            long playersAlive = this.game.participants()
                    .filter(BwParticipant::isAlive)
                    .count();
            content.writeLine(Formatting.BLUE.toString() + playersAlive + " players alive");
            content.writeLine("");

            content.writeLine(Formatting.BOLD + "Teams:");
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
                    content.writeLine("  " + nameFormat + name + ": " + descriptionFormat + state);
                } else {
                    String nameFormat = teamState.team.getFormatting().toString() + Formatting.BOLD.toString() + Formatting.STRIKETHROUGH.toString();
                    String descriptionFormat = Formatting.RESET.toString() + Formatting.RED.toString();

                    String name = teamState.team.getDisplay();
                    content.writeLine("  " + nameFormat + name + descriptionFormat + ": eliminated!");
                }
            });
        });
    }

    private static String generateTeamKey() {
        return RandomStringUtils.randomAlphanumeric(16);
    }

    @Override
    public void close() {
        MinecraftServer server = this.game.world.getServer();

        ServerScoreboard scoreboard = server.getScoreboard();
        this.scoreboardTeams.values().forEach(scoreboard::removeTeam);
    }
}
