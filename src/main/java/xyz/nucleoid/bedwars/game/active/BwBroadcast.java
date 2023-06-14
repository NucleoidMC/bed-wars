package xyz.nucleoid.bedwars.game.active;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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
        var players = this.game.playersFor(team.key());

        players.sendMessage(Text.translatable("text.bedwars.trap_set_off").formatted(Formatting.BOLD, Formatting.RED));
        this.sendTitle(players, Text.translatable("text.bedwars.title.trap_set_off").formatted(Formatting.RED), null);
        players.playSound(SoundEvents.BLOCK_BELL_USE);
    }

    public void broadcastToTeam(GameTeam team, MutableText upgradeText) {
        this.game.playersFor(team.key()).sendMessage(upgradeText);
    }

    public void broadcastGameOver(BwWinStateLogic.WinResult winResult) {
        var winningTeam = winResult.team();

        if (winningTeam != null) {
            this.game.players().sendMessage(
                    Text.translatable("text.bedwars.team_win", winningTeam.config().name()).formatted(winningTeam.config().chatFormatting(), Formatting.BOLD)
            );
        } else {
            this.game.players().sendMessage(Text.translatable("text.bedwars.draw").formatted(Formatting.BOLD));
        }
    }

    public void broadcastDeath(ServerPlayerEntity player, ServerPlayerEntity killer, DamageSource source, boolean eliminated) {
        // TODO: we can do more specific messages in the future
        MutableText announcement = Text.translatable("text.bedwars.player_death", player.getDisplayName().copy()).formatted(Formatting.GRAY);

        if (killer != null) {
            announcement = Text.translatable("text.bedwars.player_kill", player.getDisplayName().copy(), killer.getDisplayName()).formatted(Formatting.GRAY);
        }

        if (eliminated) {
            announcement = announcement
                    .append(ScreenTexts.SPACE)
                    .append(Text.translatable("text.bedwars.player_eliminated").formatted(Formatting.GRAY));
        }

        this.game.players().sendMessage(announcement);
    }

    public void broadcastBedBroken(ServerPlayerEntity player, GameTeam bedTeam, @Nullable GameTeam destroyerTeam) {
        var playerName = player.getDisplayName().copy()
                .formatted(destroyerTeam != null ? destroyerTeam.config().chatFormatting() : Formatting.OBFUSCATED);
        Text announcement = Text.translatable("text.bedwars.bed_destroyed", bedTeam.config().name(), playerName).formatted(Formatting.GRAY);

        PlayerSet players = this.game.players();
        players.sendMessage(announcement);
        players.playSound(SoundEvents.BLOCK_END_PORTAL_SPAWN);

        PlayerSet teamPlayers = this.game.playersFor(bedTeam.key());

        teamPlayers.sendMessage(Text.translatable("text.bedwars.cannot_respawn").formatted(Formatting.RED));

        this.sendTitle(
                teamPlayers,
                Text.translatable("text.bedwars.title.bed_destroyed").formatted(Formatting.RED),
                Text.translatable("text.bedwars.title.cannot_respawn").formatted(Formatting.GOLD)
        );
    }

    public void broadcastTeamEliminated(GameTeam team) {
        this.game.playersFor(team.key()).sendMessage(
                Text.translatable("text.bedwars.team_eliminated", team.config().name()).formatted(team.config().chatFormatting()).formatted(Formatting.BOLD)
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
