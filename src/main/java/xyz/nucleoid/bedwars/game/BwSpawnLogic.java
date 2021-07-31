package xyz.nucleoid.bedwars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public final class BwSpawnLogic {
    private final ServerWorld world;
    private final BwMap map;

    public BwSpawnLogic(ServerWorld world, BwMap map) {
        this.world = world;
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.getInventory().clear();
        player.getEnderChestInventory().clear();

        this.respawnPlayer(player, gameMode);
    }

    public void respawnPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.clearStatusEffects();
        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0F;
        player.setFireTicks(0);
        player.changeGameMode(gameMode);
    }

    public void spawnAtCenter(ServerPlayerEntity player) {
        Vec3d pos = this.map.getCenterSpawn();
        player.teleport(this.world, pos.x, pos.y + 0.5, pos.z, 0.0F, 0.0F);
        player.networkHandler.syncWithPlayerPosition();
    }
}
