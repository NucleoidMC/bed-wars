package xyz.nucleoid.bedwars.game.active;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.LiteralText;
import xyz.nucleoid.plasmid.widget.BossBarWidget;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

public final class BwBar {
    private final BossBarWidget widget;

    private BwBar(BossBarWidget widget) {
        this.widget = widget;
    }

    public static BwBar create(GlobalWidgets widgets) {
        return new BwBar(widgets.addBossBar(new LiteralText("Bed Wars"), BossBar.Color.GREEN, BossBar.Style.PROGRESS));
    }

    public void update(long ticksUntilBedGone, long totalTicksUntilBedGone) {
        if (ticksUntilBedGone > 0) {
            String time = this.formatTime(ticksUntilBedGone);

            this.widget.setTitle(new LiteralText("Beds destroyed in " + time + "..."));
            this.widget.setProgress((float) ticksUntilBedGone / totalTicksUntilBedGone);
        } else {
            this.widget.setTitle(new LiteralText("All beds destroyed!"));
            this.widget.setProgress(0.0F);
        }
    }

    private String formatTime(long ticksUntil) {
        long secondsUntil = ticksUntil / 20;

        long minutes = secondsUntil / 60;
        long seconds = secondsUntil % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
