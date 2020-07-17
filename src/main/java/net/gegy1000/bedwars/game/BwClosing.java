package net.gegy1000.bedwars.game;

import net.gegy1000.bedwars.game.active.BwActive;
import net.gegy1000.bedwars.game.active.BwParticipant;
import net.gegy1000.gl.game.GameTeam;
import net.gegy1000.gl.game.JoinResult;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public final class BwClosing implements BwPhase {
    public static final long CLOSE_TICKS = 10 * 20;

    private final BwActive game;
    private final GameTeam winningTeam;

    private final long closeTime;

    BwClosing(BwActive game, @Nullable GameTeam winningTeam) {
        this.game = game;
        this.winningTeam = winningTeam;
        this.closeTime = game.map.getWorld().getTime() + CLOSE_TICKS;
    }

    public boolean tick() {
        if (this.winningTeam != null) {
            this.spawnFireworks(this.winningTeam);
        }

        return this.game.map.getWorld().getTime() >= this.closeTime;
    }

    private void spawnFireworks(GameTeam team) {
        ServerWorld world = this.game.map.getWorld();
        Random random = world.random;

        if (random.nextInt(18) == 0) {
            List<ServerPlayerEntity> players = this.game.participants()
                    .map(BwParticipant::player)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            ServerPlayerEntity player = players.get(random.nextInt(players.size()));

            int flight = random.nextInt(3);
            FireworkItem.Type type = random.nextInt(4) == 0 ? FireworkItem.Type.STAR : FireworkItem.Type.BURST;
            FireworkRocketEntity firework = new FireworkRocketEntity(
                    world,
                    player.getX(),
                    player.getEyeY(),
                    player.getZ(),
                    team.createFirework(flight, type)
            );

            world.spawnEntity(firework);
        }
    }

    @Override
    public JoinResult offerPlayer(ServerPlayerEntity player) {
        return JoinResult.GAME_FULL;
    }
}
