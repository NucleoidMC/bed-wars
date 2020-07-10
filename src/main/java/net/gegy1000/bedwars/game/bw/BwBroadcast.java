package net.gegy1000.bedwars.game.bw;

import net.gegy1000.bedwars.game.GameTeam;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.annotation.Nullable;

public final class BwBroadcast {
    private final BedWars game;

    BwBroadcast(BedWars game) {
        this.game = game;
    }

    public void broadcastTrapSetOff(BwState.TeamState team) {
        this.broadcastTeam(team.team, new LiteralText("A player set off your team trap!").formatted(Formatting.BOLD, Formatting.RED));
        this.broadcastTeamSound(team.team, SoundEvents.BLOCK_BELL_USE);
    }

    public void broadcastTeamUpgrade(BwState.Participant participant, Text message) {
        ServerPlayerEntity player = participant.player();
        if (player != null) {
            Text broadcast = player.getDisplayName().shallowCopy().append(" ").append(message).formatted(Formatting.BOLD, Formatting.AQUA);
            this.broadcastTeam(participant.team, broadcast);
        } else {
            Text broadcast = new LiteralText("A player ").append(message).formatted(Formatting.BOLD, Formatting.AQUA);
            this.broadcastTeam(participant.team, broadcast);
        }
    }

    public void broadcastGameOver(BwWinStateLogic.WinResult winResult) {
        GameTeam winningTeam = winResult.getTeam();
        if (winningTeam != null) {
            this.broadcast(new LiteralText(winningTeam.getDisplay() + " team won the game!")
                    .formatted(winningTeam.getFormatting(), Formatting.BOLD)
            );
        } else {
            this.broadcast(new LiteralText("The game ended in a draw!").formatted(Formatting.BOLD));
        }
    }

    public void broadcastDeath(ServerPlayerEntity player, DamageSource source, boolean eliminated) {
        Entity attacker = source.getAttacker();
        if (attacker != null) {
            Text deathAnnouncement = player.getDisplayName().shallowCopy()
                    .append(new LiteralText(eliminated ? " was eliminated by " : " was killed by ").formatted(Formatting.GRAY))
                    .append(attacker.getDisplayName())
                    .formatted(Formatting.ITALIC);
            this.broadcast(deathAnnouncement);
        } else {
            Text deathAnnouncement = player.getDisplayName().shallowCopy()
                    .append(new LiteralText(eliminated ? " was eliminated" : " died").formatted(Formatting.GRAY))
                    .formatted(Formatting.ITALIC);
            this.broadcast(deathAnnouncement);
        }
    }

    public void broadcastBedBroken(ServerPlayerEntity player, GameTeam bedTeam, @Nullable GameTeam destroyerTeam) {
        Text announcement = new LiteralText(bedTeam.getDisplay()).formatted(bedTeam.getFormatting())
                .append(new LiteralText(" bed was destroyed by ").formatted(Formatting.GRAY))
                .append(player.getDisplayName().shallowCopy().formatted(destroyerTeam != null ? destroyerTeam.getFormatting() : Formatting.OBFUSCATED));

        this.broadcast(announcement);
        this.broadcastSound(SoundEvents.BLOCK_END_PORTAL_SPAWN);

        this.broadcastTeam(bedTeam, new LiteralText("Your bed has been destroyed! You can no longer respawn!").formatted(Formatting.RED, Formatting.BOLD));
    }

    public void broadcastTeamEliminated(GameTeam team) {
        this.broadcast(new LiteralText(team.getDisplay()).formatted(team.getFormatting())
                .append(new LiteralText(" Team was eliminated!").formatted(Formatting.BOLD))
        );
    }

    public void broadcastTeam(GameTeam team, Text message) {
        this.game.state.participantsFor(team).forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player != null) {
                player.sendMessage(message, false);
            }
        });
    }

    public void broadcast(Text message) {
        this.game.state.participants().forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player != null) {
                player.sendMessage(message, false);
            }
        });
    }

    public void broadcastSound(SoundEvent sound) {
        this.game.state.participants().forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player != null) {
                player.playSound(sound, SoundCategory.MASTER, 1.0F, 1.0F);
            }
        });
    }

    public void broadcastTeamSound(GameTeam team, SoundEvent sound) {
        this.game.state.participantsFor(team).forEach(participant -> {
            ServerPlayerEntity player = participant.player();
            if (player != null) {
                player.playSound(sound, SoundCategory.MASTER, 1.0F, 1.0F);
            }
        });
    }
}
