package xyz.nucleoid.bedwars.game.active;

import net.minecraft.world.BossEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import xyz.nucleoid.plasmid.api.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.api.game.common.widget.BossBarWidget;

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
            this.countdown = countdown = this.widgets.addBossBar(CommonComponents.EMPTY, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        }

        if (countdown == null) {
            return;
        }

        long timeUntil = TIME - time;
        if (timeUntil > 0) {
            countdown.setTitle(Component.translatable("text.bedwars.bar.beds_cooldown", this.formatTime(timeUntil)));
            countdown.setProgress((float) timeUntil / WARN_TIME);
        } else {
            countdown.setTitle(Component.translatable("text.bedwars.bar.beds_destroyed"));
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
