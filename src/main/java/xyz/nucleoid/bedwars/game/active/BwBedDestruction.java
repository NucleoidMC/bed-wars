package xyz.nucleoid.bedwars.game.active;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.common.widget.BossBarWidget;

public final class BwBedDestruction {
    public static final long TIME = 20 * 60 * 20;
    private static final long WARN_TIME = 20 * 60;

    private final GlobalWidgets widgets;

    private BossBarWidget countdown;
    private boolean destroyed;

    public BwBedDestruction(GlobalWidgets widgets) {
        this.widgets = widgets;
    }

    public boolean update(long time) {
        if (this.destroyed) {
            return false;
        }

        if (time % 20 == 0) {
            this.updateCountdown(time);
        }

        if (time >= TIME) {
            this.destroyed = true;
            return true;
        } else {
            return false;
        }
    }

    private void updateCountdown(long time) {
        var countdown = this.countdown;
        if (countdown == null && time >= TIME - WARN_TIME) {
            this.countdown = countdown = this.widgets.addBossBar(ScreenTexts.EMPTY, BossBar.Color.RED, BossBar.Style.PROGRESS);
        }

        if (countdown == null) {
            return;
        }

        long timeUntil = TIME - time;
        if (timeUntil > 0) {
            countdown.setTitle(Text.translatable("text.bedwars.bar.beds_cooldown", this.formatTime(timeUntil)));
            countdown.setProgress((float) timeUntil / WARN_TIME);
        } else {
            countdown.setTitle(Text.translatable("text.bedwars.bar.beds_destroyed"));
            countdown.setProgress(0.0F);
        }
    }

    private String formatTime(long ticksUntil) {
        long secondsUntil = ticksUntil / 20;

        long minutes = secondsUntil / 60;
        long seconds = secondsUntil % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
