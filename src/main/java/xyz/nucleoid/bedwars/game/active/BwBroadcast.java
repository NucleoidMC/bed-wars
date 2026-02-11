package xyz.nucleoid.bedwars.game.active;

import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.player.PlayerSet;

public final class BwBroadcast {
    private final BwActive game;

    BwBroadcast(BwActive game) {
        this.game = game;
    }

    public void broadcastTrapSetOff(GameTeam team) {
        var players = this.game.playersFor(team.key());

        players.sendMessage(Component.translatable("text.bedwars.trap_set_off").withStyle(ChatFormatting.BOLD, ChatFormatting.RED));
        this.sendTitle(players, Component.translatable("text.bedwars.title.trap_set_off").withStyle(ChatFormatting.RED), null);
        players.playSound(SoundEvents.BELL_BLOCK);
    }

    public void broadcastToTeam(GameTeam team, MutableComponent upgradeText) {
        this.game.playersFor(team.key()).sendMessage(upgradeText);
    }

    public void broadcastGameOver(BwWinStateLogic.WinResult winResult) {
        var winningTeam = winResult.team();

        if (winningTeam != null) {
            this.game.players().sendMessage(
                    Component.translatable("text.bedwars.team_win", winningTeam.config().name()).withStyle(winningTeam.config().chatFormatting(), ChatFormatting.BOLD)
            );
        } else {
            this.game.players().sendMessage(Component.translatable("text.bedwars.draw").withStyle(ChatFormatting.BOLD));
        }
    }

    public void broadcastDeath(ServerPlayer player, ServerPlayer killer, boolean eliminated) {
        // TODO: we can do more specific messages in the future
        MutableComponent announcement = Component.translatable("text.bedwars.player_death", player.getDisplayName().copy()).withStyle(ChatFormatting.GRAY);

        if (killer != null) {
            announcement = Component.translatable("text.bedwars.player_kill", player.getDisplayName().copy(), killer.getDisplayName()).withStyle(ChatFormatting.GRAY);
        }

        if (eliminated) {
            announcement
                    .append(CommonComponents.SPACE)
                    .append(Component.translatable("text.bedwars.player_eliminated").withStyle(ChatFormatting.GRAY));
        }

        this.game.players().sendMessage(announcement);
    }

    public void broadcastBedBroken(ServerPlayer player, GameTeam bedTeam, @Nullable GameTeam destroyerTeam) {
        var playerName = player.getDisplayName().copy()
                .withStyle(destroyerTeam != null ? destroyerTeam.config().chatFormatting() : ChatFormatting.OBFUSCATED);
        Component announcement = Component.translatable("text.bedwars.bed_destroyed", bedTeam.config().name(), playerName).withStyle(ChatFormatting.GRAY);

        PlayerSet players = this.game.players();
        players.sendMessage(announcement);
        players.playSound(SoundEvents.END_PORTAL_SPAWN);

        PlayerSet teamPlayers = this.game.playersFor(bedTeam.key());

        teamPlayers.sendMessage(Component.translatable("text.bedwars.cannot_respawn").withStyle(ChatFormatting.RED));

        this.sendTitle(
                teamPlayers,
                Component.translatable("text.bedwars.title.bed_destroyed").withStyle(ChatFormatting.RED),
                Component.translatable("text.bedwars.title.cannot_respawn").withStyle(ChatFormatting.GOLD)
        );
    }

    public void broadcastTeamEliminated(GameTeam team) {
        this.game.playersFor(team.key()).sendMessage(
                Component.translatable("text.bedwars.team_eliminated", team.config().name()).withStyle(team.config().chatFormatting()).withStyle(ChatFormatting.BOLD)
        );
    }

    public void sendTitle(PlayerSet players, Component title, Component subtitle) {
        if (title != null) {
            players.sendPacket(new ClientboundSetTitleTextPacket(title));
        }

        if (subtitle != null) {
            players.sendPacket(new ClientboundSetSubtitleTextPacket(subtitle));
        }
    }
}
