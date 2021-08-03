package xyz.nucleoid.bedwars.game.active;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

public final class BwBroadcast {
    private final BwActive game;

    BwBroadcast(BwActive game) {
        this.game = game;
    }

    public void broadcastTrapSetOff(GameTeam team) {
        var players = this.game.playersFor(team);

        players.sendMessage(new TranslatableText("text.bedwars.trap_set_off").formatted(Formatting.BOLD, Formatting.RED));
        this.sendTitle(players, new TranslatableText("text.bedwars.title.trap_set_off").formatted(Formatting.RED), null);
        players.playSound(SoundEvents.BLOCK_BELL_USE);
    }

    public void broadcastToTeam(GameTeam team, MutableText upgradeText) {
        this.game.playersFor(team).sendMessage(upgradeText);
    }

    public void broadcastGameOver(BwWinStateLogic.WinResult winResult) {
        GameTeam winningTeam = winResult.team();
        if (winningTeam != null) {
            this.game.players().sendMessage(
                    new TranslatableText("text.bedwars.team_win", winningTeam.display()).formatted(winningTeam.formatting(), Formatting.BOLD)
            );
        } else {
            this.game.players().sendMessage(new TranslatableText("text.bedwars.draw").formatted(Formatting.BOLD));
        }
    }

    public void broadcastDeath(ServerPlayerEntity player, ServerPlayerEntity killer, DamageSource source, boolean eliminated) {
        // TODO: we can do more specific messages in the future
        MutableText announcement = new TranslatableText("text.bedwars.player_death", player.getDisplayName().shallowCopy()).formatted(Formatting.GRAY);

        if (killer != null) {
            announcement = new TranslatableText("text.bedwars.player_kill", player.getDisplayName().shallowCopy(), killer.getDisplayName()).formatted(Formatting.GRAY);
        }

        if (eliminated) {
            announcement = announcement.append(new TranslatableText("text.bedwars.player_eliminated").formatted(Formatting.GRAY));
        }

        this.game.players().sendMessage(announcement);
    }

    public void broadcastBedBroken(ServerPlayerEntity player, GameTeam bedTeam, @Nullable GameTeam destroyerTeam) {
        Text announcement = new TranslatableText("text.bedwars.bed_destroyed", bedTeam.display().shallowCopy().formatted(bedTeam.formatting()), player.getDisplayName().shallowCopy().formatted(destroyerTeam != null ? destroyerTeam.formatting() : Formatting.OBFUSCATED)).formatted(Formatting.GRAY);

        PlayerSet players = this.game.players();
        players.sendMessage(announcement);
        players.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN);

        PlayerSet teamPlayers = this.game.playersFor(bedTeam);

        teamPlayers.sendMessage(new TranslatableText("text.bedwars.cannot_respawn").formatted(Formatting.RED));

        this.sendTitle(
                teamPlayers,
                new TranslatableText("text.bedwars.title.bed_destroyed").formatted(Formatting.RED),
                new TranslatableText("text.bedwars.title.cannot_respawn").formatted(Formatting.GOLD)
        );
    }

    public void broadcastTeamEliminated(GameTeam team) {
        this.game.playersFor(team).sendMessage(
                new TranslatableText("text.bedwars.team_eliminated", team.display()).formatted(team.formatting()).formatted(Formatting.BOLD)
        );
    }

    public void sendTitle(PlayerSet players, Text title, Text subtitle) {
        if (title != null) {
            players.sendPacket(new TitleS2CPacket(title));
        }

        if (subtitle != null) {
            players.sendPacket(new SubtitleS2CPacket(subtitle));
        }
    }
}
