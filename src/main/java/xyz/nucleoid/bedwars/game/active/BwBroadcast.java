package xyz.nucleoid.bedwars.game.active;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

public final class BwBroadcast {
    private final BwActive game;

    BwBroadcast(BwActive game) {
        this.game = game;
    }

    public void broadcastTrapSetOff(BwActive.TeamState team) {
        MutablePlayerSet players = team.players;

        players.sendMessage(new TranslatableText("text.bedwars.trap_set_off").formatted(Formatting.BOLD, Formatting.RED));
        this.sendTitle(players, new TranslatableText("title.bedwars.trap_set_off").formatted(Formatting.RED), null);
        players.sendSound(SoundEvents.BLOCK_BELL_USE);
    }

    public void broadcastToTeam(GameTeam team, MutableText upgradeText) {
        this.game.playersFor(team).sendMessage(upgradeText);
    }

    public void broadcastGameOver(BwWinStateLogic.WinResult winResult) {
        GameTeam winningTeam = winResult.getTeam();
        if (winningTeam != null) {
            this.game.players().sendMessage(
                    new TranslatableText("text.bedwars.team_win", winningTeam.getDisplay()).formatted(winningTeam.getFormatting(), Formatting.BOLD)
            );
        } else {
            this.game.players().sendMessage(new TranslatableText("text.bedwars.draw").formatted(Formatting.BOLD));
        }
    }

    public void broadcastDeath(ServerPlayerEntity player, ServerPlayerEntity killer, DamageSource source, boolean eliminated) {
        // TODO: we can do more specific messages in the future
        MutableText announcement;

        if (killer == null) {
            announcement = new TranslatableText("text.bedwars.player_death", player.getDisplayName().shallowCopy()).formatted(Formatting.GRAY);
        }
        else {
            announcement = new TranslatableText("text.bedwars.player_kill", player.getDisplayName().shallowCopy(), killer.getDisplayName()).formatted(Formatting.GRAY);
        }

        if (eliminated) {
            announcement = announcement.append(new TranslatableText("text.bedwars.player_eliminated").formatted(Formatting.GRAY));
        }

        this.game.players().sendMessage(announcement);
    }

    public void broadcastBedBroken(ServerPlayerEntity player, GameTeam bedTeam, @Nullable GameTeam destroyerTeam) {
        Text announcement = new TranslatableText("text.bedwars.bed_destroyed", new LiteralText(bedTeam.getDisplay()).formatted(bedTeam.getFormatting()), player.getDisplayName().shallowCopy().formatted(destroyerTeam != null ? destroyerTeam.getFormatting() : Formatting.OBFUSCATED)).formatted(Formatting.GRAY);

        PlayerSet players = this.game.players();
        players.sendMessage(announcement);
        players.sendSound(SoundEvents.BLOCK_END_PORTAL_SPAWN);

        PlayerSet teamPlayers = this.game.playersFor(bedTeam);

        teamPlayers.sendMessage(new TranslatableText("text.bedwars.cannot_respawn").formatted(Formatting.RED));

        this.sendTitle(
                teamPlayers,
                new TranslatableText("title.bedwars.bed_destroyed").formatted(Formatting.RED),
                new LiteralText("title.bedwars.cannot_respawn").formatted(Formatting.GOLD)
        );
    }

    public void broadcastTeamEliminated(GameTeam team) {
        this.game.playersFor(team).sendMessage(
                new TranslatableText("text.bedwars.team_eliminated", new LiteralText(team.getDisplay()).formatted(team.getFormatting())).formatted(Formatting.BOLD)
        );
    }

    public void sendTitle(PlayerSet players, Text title, Text subtitle) {
        if (title != null) {
            players.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, title));
        }

        if (subtitle != null) {
            players.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, subtitle));
        }
    }
}
