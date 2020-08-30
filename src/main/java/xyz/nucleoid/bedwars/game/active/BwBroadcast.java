package xyz.nucleoid.bedwars.game.active;

import net.minecraft.text.TranslatableText;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
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

        this.broadcast(target, new TranslatableText("text.bedwars.game.team.trap.player").formatted(Formatting.BOLD, Formatting.RED));
        this.broadcastTitle(target, new TranslatableText("text.bedwars.game.team.trap.enabled").formatted(Formatting.RED), null);
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
            this.broadcast(this.everyone(), new TranslatableText("text.bedwars.game.win", winningTeam.getDisplay())
                    .formatted(winningTeam.getFormatting(), Formatting.BOLD)
            );
        } else {
            this.broadcast(this.everyone(), new TranslatableText("text.bedwars.game.win.nobody").formatted(Formatting.BOLD));
        }
    }

    public void broadcastDeath(ServerPlayerEntity player, ServerPlayerEntity killer, DamageSource source, boolean eliminated) {
        // TODO: we can do more specific messages in the future
        MutableText announcement = new TranslatableText("text.bedwars.game.killed", player.getDisplayName()).formatted(Formatting.GRAY);

        if (eliminated) {
            if (killer != null) {
                announcement = new TranslatableText("text.bedwars.game.killed.eliminated.player", player.getDisplayName(), killer.getDisplayName()).formatted(Formatting.GRAY);
            } else {
                announcement = new TranslatableText("text.bedwars.game.killed.eliminated", player.getDisplayName()).formatted(Formatting.GRAY);
            }
        } else if (killer != null) {
            announcement = new TranslatableText("text.bedwars.game.killed.player", player.getDisplayName(), killer.getDisplayName()).formatted(Formatting.GRAY);
        }

        this.broadcast(this.everyone(), announcement);
    }

    public void broadcastBedBroken(ServerPlayerEntity player, GameTeam bedTeam, @Nullable GameTeam destroyerTeam) {
        Text announcement = new TranslatableText("text.bedwars.game.bed_destroyed", bedTeam.getDisplay(),
                player.getDisplayName().shallowCopy().formatted(destroyerTeam != null ? destroyerTeam.getFormatting() : Formatting.OBFUSCATED))
                .formatted(Formatting.GRAY);

        this.broadcast(this.everyone(), announcement);
        this.broadcastSound(this.everyone(), SoundEvents.BLOCK_END_PORTAL_SPAWN);

        Target teamTarget = this.team(bedTeam);

        this.broadcast(teamTarget, new TranslatableText("text.bedwars.game.bed_destroyed.team").formatted(Formatting.RED));

        this.broadcastTitle(
                teamTarget,
                new TranslatableText("text.bedwars.game.bed_destroyed.title").formatted(Formatting.RED),
                new TranslatableText("text.bedwars.game.bed_destroyed.title.sub").formatted(Formatting.GOLD)
        );
    }

    public void broadcastTeamEliminated(GameTeam team) {
        this.broadcast(this.team(team),
                new TranslatableText("text.bedwars.game.team.eliminated",
                        new LiteralText(team.getDisplay()).formatted(team.getFormatting()))
                        .formatted(Formatting.BOLD)
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
