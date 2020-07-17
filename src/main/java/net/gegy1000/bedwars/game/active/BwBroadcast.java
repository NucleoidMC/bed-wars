package net.gegy1000.bedwars.game.active;

import net.gegy1000.gl.game.GameTeam;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class BwBroadcast {
    private final BwActive game;

    BwBroadcast(BwActive game) {
        this.game = game;
    }

    public void broadcastTrapSetOff(BwActive.TeamState team) {
        Target target = this.team(team.team);

        this.broadcast(target, new LiteralText("A player set off your team trap!").formatted(Formatting.BOLD, Formatting.RED));
        this.broadcastTitle(target, new LiteralText("Trap activated!").formatted(Formatting.RED), null);
        this.broadcastSound(target, SoundEvents.BLOCK_BELL_USE);
    }

    public void broadcastTeamUpgrade(BwParticipant participant, Text message) {
        ServerPlayerEntity player = participant.player();

        Text broadcast;
        if (player != null) {
            broadcast = player.getDisplayName().shallowCopy().append(" ").append(message).formatted(Formatting.BOLD, Formatting.AQUA);
        } else {
            broadcast = new LiteralText("A player ").append(message).formatted(Formatting.BOLD, Formatting.AQUA);
        }

        this.broadcast(this.team(participant.team), broadcast);
    }

    public void broadcastGameOver(BwWinStateLogic.WinResult winResult) {
        GameTeam winningTeam = winResult.getTeam();
        if (winningTeam != null) {
            this.broadcast(this.everyone(), new LiteralText(winningTeam.getDisplay() + " team won the game!")
                    .formatted(winningTeam.getFormatting(), Formatting.BOLD)
            );
        } else {
            this.broadcast(this.everyone(), new LiteralText("The game ended in a draw!").formatted(Formatting.BOLD));
        }
    }

    public void broadcastDeath(ServerPlayerEntity player, DamageSource source, boolean eliminated) {
        Entity attacker = source.getAttacker();
        Text deathAnnouncement;
        if (attacker != null) {
            deathAnnouncement = player.getDisplayName().shallowCopy()
                    .append(new LiteralText(eliminated ? " was eliminated by " : " was killed by ").formatted(Formatting.GRAY))
                    .append(attacker.getDisplayName())
                    .formatted(Formatting.ITALIC);
        } else {
            deathAnnouncement = player.getDisplayName().shallowCopy()
                    .append(new LiteralText(eliminated ? " was eliminated" : " died").formatted(Formatting.GRAY))
                    .formatted(Formatting.ITALIC);
        }

        this.broadcast(this.everyone(), deathAnnouncement);
    }

    public void broadcastBedBroken(ServerPlayerEntity player, GameTeam bedTeam, @Nullable GameTeam destroyerTeam) {
        Text announcement = new LiteralText(bedTeam.getDisplay()).formatted(bedTeam.getFormatting())
                .append(new LiteralText(" bed was destroyed by ").formatted(Formatting.GRAY))
                .append(player.getDisplayName().shallowCopy().formatted(destroyerTeam != null ? destroyerTeam.getFormatting() : Formatting.OBFUSCATED));

        this.broadcast(this.everyone(), announcement);
        this.broadcastSound(this.everyone(), SoundEvents.BLOCK_END_PORTAL_SPAWN);

        Target teamTarget = this.team(bedTeam);

        this.broadcast(teamTarget, new LiteralText("Your bed has been destroyed! You can no longer respawn!").formatted(Formatting.RED));

        this.broadcastTitle(
                teamTarget,
                new LiteralText("Bed destroyed!").formatted(Formatting.RED),
                new LiteralText("You can no longer respawn!").formatted(Formatting.GOLD)
        );
    }

    public void broadcastTeamEliminated(GameTeam team) {
        this.broadcast(this.team(team),
                new LiteralText(team.getDisplay()).formatted(team.getFormatting())
                        .append(new LiteralText(" Team was eliminated!").formatted(Formatting.BOLD))
        );
    }

    public void broadcast(Target target, Text message) {
        target.players().forEach(player -> {
            player.sendMessage(message, false);
        });
    }

    public void broadcastSound(Target target, SoundEvent sound) {
        target.players().forEach(player -> {
            player.playSound(sound, SoundCategory.MASTER, 1.0F, 1.0F);
        });
    }

    public void broadcastTitle(Target target, Text title, Text subtitle) {
        TitleS2CPacket titlePacket = title != null ? new TitleS2CPacket(TitleS2CPacket.Action.TITLE, title) : null;
        TitleS2CPacket subtitlePacket = subtitle != null ? new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, subtitle) : null;

        target.players().forEach(player -> {
            if (titlePacket != null) player.networkHandler.sendPacket(titlePacket);
            if (subtitlePacket != null) player.networkHandler.sendPacket(subtitlePacket);
        });
    }

    private void broadcast(Target target, Consumer<ServerPlayerEntity> consumer) {
        target.players().forEach(consumer);
    }

    public Target everyone() {
        return this.game::players;
    }

    public Target team(GameTeam team) {
        return () -> {
            return this.game.participantsFor(team)
                    .map(BwParticipant::player)
                    .filter(Objects::nonNull);
        };
    }

    public interface Target {
        Stream<ServerPlayerEntity> players();
    }
}
