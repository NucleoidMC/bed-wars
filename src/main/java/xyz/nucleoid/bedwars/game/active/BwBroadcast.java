package xyz.nucleoid.bedwars.game.active;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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

        players.sendMessage(new LiteralText("A player set off your team trap!").formatted(Formatting.BOLD, Formatting.RED));
        this.sendTitle(players, new LiteralText("Trap activated!").formatted(Formatting.RED), null);
        players.sendSound(SoundEvents.BLOCK_BELL_USE);
    }

    public void broadcastTeamUpgrade(BwParticipant participant, Text message) {
        ServerPlayerEntity player = participant.player();

        Text broadcast;
        if (player != null) {
            broadcast = player.getDisplayName().shallowCopy().append(" ").append(message).formatted(Formatting.BOLD, Formatting.AQUA);
        } else {
            broadcast = new LiteralText("A player ").append(message).formatted(Formatting.BOLD, Formatting.AQUA);
        }

        this.game.playersFor(participant.team).sendMessage(broadcast);
    }

    public void broadcastGameOver(BwWinStateLogic.WinResult winResult) {
        GameTeam winningTeam = winResult.getTeam();
        if (winningTeam != null) {
            this.game.players().sendMessage(
                    new LiteralText(winningTeam.getDisplay() + " team won the game!")
                            .formatted(winningTeam.getFormatting(), Formatting.BOLD)
            );
        } else {
            this.game.players().sendMessage(new LiteralText("The game ended in a draw!").formatted(Formatting.BOLD));
        }
    }

    public void broadcastDeath(ServerPlayerEntity player, ServerPlayerEntity killer, DamageSource source, boolean eliminated) {
        // TODO: we can do more specific messages in the future
        MutableText announcement = player.getDisplayName().shallowCopy()
                .append(new LiteralText(" was killed").formatted(Formatting.GRAY));

        if (killer != null) {
            announcement = announcement.append(new LiteralText(" by ").formatted(Formatting.GRAY)).append(killer.getDisplayName());
        }

        if (eliminated) {
            announcement = announcement.append(new LiteralText(". They are now eliminated!").formatted(Formatting.GRAY));
        }

        this.game.players().sendMessage(announcement);
    }

    public void broadcastBedBroken(ServerPlayerEntity player, GameTeam bedTeam, @Nullable GameTeam destroyerTeam) {
        Text announcement = new LiteralText(bedTeam.getDisplay()).formatted(bedTeam.getFormatting())
                .append(new LiteralText(" bed was destroyed by ").formatted(Formatting.GRAY))
                .append(player.getDisplayName().shallowCopy().formatted(destroyerTeam != null ? destroyerTeam.getFormatting() : Formatting.OBFUSCATED));

        PlayerSet players = this.game.players();
        players.sendMessage(announcement);
        players.sendSound(SoundEvents.BLOCK_END_PORTAL_SPAWN);

        PlayerSet teamPlayers = this.game.playersFor(bedTeam);

        teamPlayers.sendMessage(new LiteralText("Your bed has been destroyed! You can no longer respawn!").formatted(Formatting.RED));

        this.sendTitle(
                teamPlayers,
                new LiteralText("Bed destroyed!").formatted(Formatting.RED),
                new LiteralText("You can no longer respawn!").formatted(Formatting.GOLD)
        );
    }

    public void broadcastTeamEliminated(GameTeam team) {
        this.game.playersFor(team).sendMessage(
                new LiteralText(team.getDisplay()).formatted(team.getFormatting())
                        .append(new LiteralText(" Team was eliminated!").formatted(Formatting.BOLD))
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
