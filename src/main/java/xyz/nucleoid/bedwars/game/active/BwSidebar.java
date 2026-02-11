package xyz.nucleoid.bedwars.game.active;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.SidebarWidget;

public final class BwSidebar {
    private final BwActive game;

    private final SidebarWidget sidebar;

    private long ticks;

    BwSidebar(BwActive game, SidebarWidget sidebar) {
        this.game = game;
        this.sidebar = sidebar;
    }

    public static BwSidebar create(BwActive game, GlobalWidgets widgets) {
        SidebarWidget sidebar = widgets.addSidebar(Component.translatable("gameType.bedwars.bed_wars").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
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

            var timer = Component.literal(String.format("%02d:%02d", minutes, seconds)).withStyle(ChatFormatting.WHITE);
            content.add(Component.literal("Time: ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD).append(timer));

            long playersAlive = this.game.participants()
                    .filter(BwParticipant::isAlive)
                    .count();
            content.add(Component.literal(playersAlive + " players alive").withStyle(ChatFormatting.BLUE));
            content.add(CommonComponents.EMPTY);

            content.add(Component.literal("Teams:").withStyle(ChatFormatting.BOLD));
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

                    Component name = team.config().name().copy()
                            .withStyle(ChatFormatting.BOLD);
                    Component description = Component.literal(": " + state)
                            .withStyle(ChatFormatting.GRAY);
                    content.add(Component.literal("  ").append(name).append(description));
                } else {
                    Component name = team.config().name().copy()
                            .withStyle(ChatFormatting.BOLD, ChatFormatting.STRIKETHROUGH);
                    Component description = Component.literal(": eliminated!")
                            .withStyle(ChatFormatting.RED);
                    content.add(Component.literal("  ").append(name).append(description));
                }
            });
        });
    }
}
