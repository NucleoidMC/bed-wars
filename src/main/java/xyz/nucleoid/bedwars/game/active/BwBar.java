package xyz.nucleoid.bedwars.game.active;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.game.GameWorld;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.widget.BossBarWidget;

public final class BwBar implements AutoCloseable {
    private final BossBarWidget bar;

    public BwBar(GameWorld world) {
        PlayerSet players = world.getPlayerSet();
        this.bar = BossBarWidget.open(players, new LiteralText("Bed Wars"), BossBar.Color.GREEN, BossBar.Style.PROGRESS);
    }

    public void update(long ticksUntilBedGone, long totalTicksUntilBedGone) {
        if (ticksUntilBedGone > 0) {
            String time = this.formatTime(ticksUntilBedGone);

            this.bar.setTitle(new LiteralText("Beds destroyed in " + time + "..."));
            this.bar.setProgress((float) ticksUntilBedGone / totalTicksUntilBedGone);
        } else {
            this.bar.setTitle(new LiteralText("All beds destroyed!"));
            this.bar.setProgress(0.0F);
        }
    }

    private String formatTime(long ticksUntil) {
        long secondsUntil = ticksUntil / 20;

        long minutes = secondsUntil / 60;
        long seconds = secondsUntil % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void close() {
        this.bar.close();
    }
}
