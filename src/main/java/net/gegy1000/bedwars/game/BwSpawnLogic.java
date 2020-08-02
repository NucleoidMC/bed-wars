package net.gegy1000.bedwars.game;

import net.gegy1000.gl.game.map.GameMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;

public final class BwSpawnLogic {
    private final GameMap map;

    public BwSpawnLogic(GameMap map) {
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.getEnderChestInventory().clear();
        this.respawnPlayer(player, gameMode);
    }

    public void respawnPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.inventory.clear();
        player.clearStatusEffects();
        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0F;
        player.setGameMode(gameMode);
    }

    public void spawnAtCenter(ServerPlayerEntity player) {
        ServerWorld world = this.map.getWorld();

        BlockPos center = new BlockPos(this.map.getBounds().getCenter());
        int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, center.getX(), center.getZ());

        player.teleport(world, center.getX() + 0.5, topY + 0.5, center.getZ() + 0.5, 0.0F, 0.0F);
    }
}
