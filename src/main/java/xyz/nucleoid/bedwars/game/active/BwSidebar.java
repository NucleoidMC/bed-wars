package xyz.nucleoid.bedwars.game.active;

import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.SidebarWidget;

public final class BwSidebar {
    private final BwActive game;

    private final SidebarWidget sidebar;

    private long ticks;

    BwSidebar(BwActive game, SidebarWidget sidebar) {
        this.game = game;
        this.sidebar = sidebar;
    }

    public static BwSidebar create(BwActive game, GlobalWidgets widgets) {
        SidebarWidget sidebar = widgets.addSidebar(Text.translatable("gameType.bedwars.bed_wars").formatted(Formatting.GOLD, Formatting.BOLD));
        return new BwSidebar(game, sidebar);
    }

    public void tick() {
        if (this.ticks++ % 20 == 0) {
            this.render();
        }
    }

    private void render() {
        this.sidebar.set(content -> {
            long seconds = (this.ticks / 20) % 60;
            long minutes = this.ticks / (20 * 60);

            var timer = Text.literal(String.format("%02d:%02d", minutes, seconds)).formatted(Formatting.WHITE);
            content.add(Text.literal("Time: ").formatted(Formatting.RED, Formatting.BOLD).append(timer));

            long playersAlive = this.game.participants()
                    .filter(BwParticipant::isAlive)
                    .count();
            content.add(Text.literal(playersAlive + " players alive").formatted(Formatting.BLUE));
            content.add(ScreenTexts.EMPTY);

            content.add(Text.literal("Teams:").formatted(Formatting.BOLD));
            this.game.teamsStates().forEach(teamState -> {
                var team = teamState.team;

                long totalPlayerCount = this.game.participantsFor(team.key()).count();
                long alivePlayerCount = this.game.participantsFor(team.key())
                        .filter(BwParticipant::isAlive)
                        .count();

                if (!teamState.eliminated) {
                    String state = alivePlayerCount + "/" + totalPlayerCount;
                    if (!teamState.hasBed) {
                        state += " (no bed)";
                    }

                    Text name = team.config().name().copy()
                            .formatted(Formatting.BOLD);
                    Text description = Text.literal(": " + state)
                            .formatted(Formatting.GRAY);
                    content.add(Text.literal("  ").append(name).append(description));
                } else {
                    Text name = team.config().name().copy()
                            .formatted(Formatting.BOLD, Formatting.STRIKETHROUGH);
                    Text description = Text.literal(": eliminated!")
                            .formatted(Formatting.RED);
                    content.add(Text.literal("  ").append(name).append(description));
                }
            });
        });
    }
}
