package xyz.nucleoid.bedwars.game.active;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
        SidebarWidget sidebar = widgets.addSidebar(new TranslatableText("game.bedwars.bed_wars").formatted(Formatting.GOLD, Formatting.BOLD));
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

            var timer = new LiteralText(String.format("%02d:%02d", minutes, seconds)).formatted(Formatting.WHITE);
            content.add(new LiteralText("Time: ").formatted(Formatting.RED, Formatting.BOLD).append(timer));

            long playersAlive = this.game.participants()
                    .filter(BwParticipant::isAlive)
                    .count();
            content.add(new LiteralText(playersAlive + " players alive").formatted(Formatting.BLUE));
            content.add(LiteralText.EMPTY);

            content.add(new LiteralText("Teams:").formatted(Formatting.BOLD));
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

                    Text name = teamState.config.name().shallowCopy()
                            .formatted(Formatting.BOLD);
                    Text description = new LiteralText(": " + state)
                            .formatted(Formatting.GRAY);
                    content.add(new LiteralText("  ").append(name).append(description));
                } else {
                    Text name = teamState.config.name().shallowCopy()
                            .formatted(Formatting.BOLD, Formatting.STRIKETHROUGH);
                    Text description = new LiteralText(": eliminated!")
                            .formatted(Formatting.RED);
                    content.add(new LiteralText("  ").append(name).append(description));
                }
            });
        });
    }
}
