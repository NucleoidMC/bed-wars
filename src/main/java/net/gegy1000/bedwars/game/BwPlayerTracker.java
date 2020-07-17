package net.gegy1000.bedwars.game;

import net.gegy1000.gl.game.player.PlayerSnapshot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TODO: this can be extracted into generic Game object
public final class BwPlayerTracker {
    private final BwMap map;
    private final Map<UUID, PlayerSnapshot> playerSnapshots = new HashMap<>();

    public BwPlayerTracker(BwMap map) {
        this.map = map;
    }

    public void joinPlayer(ServerPlayerEntity player) {
        if (!this.playerSnapshots.containsKey(player.getUuid())) {
            this.playerSnapshots.put(player.getUuid(), PlayerSnapshot.take(player));
        }

        BedWars.resetPlayer(player);
        player.getEnderChestInventory().clear();
    }

    public void restorePlayers() {
        this.playerSnapshots.forEach((uuid, snapshot) -> {
            // TODO: restore if offline
            PlayerEntity player = this.map.getWorld().getPlayerByUuid(uuid);
            if (player != null && player instanceof ServerPlayerEntity) {
                snapshot.restore((ServerPlayerEntity) player);
            }
        });
    }

    public void spawnAtCenter(ServerPlayerEntity player, GameMode mode) {
        BedWars.resetPlayer(player);
        player.setGameMode(mode);

        ServerWorld world = this.map.getWorld();

        BlockPos center = new BlockPos(this.map.getCenter());
        int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, center.getX(), center.getZ());

        player.teleport(world, center.getX() + 0.5, topY + 0.5, center.getZ() + 0.5, 0.0F, 0.0F);
    }
}
