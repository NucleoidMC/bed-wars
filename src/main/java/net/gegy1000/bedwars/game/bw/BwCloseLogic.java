package net.gegy1000.bedwars.game.bw;

import net.gegy1000.bedwars.game.GameTeam;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public final class BwCloseLogic {
    public static final long CLOSE_TICKS = 10 * 20;

    private final BedWars game;
    private final GameTeam winningTeam;

    private final long closeTime;

    BwCloseLogic(BedWars game, @Nullable GameTeam winningTeam) {
        this.game = game;
        this.winningTeam = winningTeam;
        this.closeTime = game.world.getTime() + CLOSE_TICKS;
    }

    private void spawnFireworks(GameTeam team) {
        ServerWorld world = this.game.world;
        Random random = world.random;

        if (random.nextInt(18) == 0) {
            List<ServerPlayerEntity> players = this.game.state.participants()
                    .map(BwState.Participant::player)
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

    public boolean tick() {
        if (this.winningTeam != null) {
            this.spawnFireworks(this.winningTeam);
        }

        return this.game.world.getTime() >= this.closeTime;
    }
}
