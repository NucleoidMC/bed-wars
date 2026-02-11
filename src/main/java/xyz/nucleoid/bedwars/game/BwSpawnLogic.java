package xyz.nucleoid.bedwars.game;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameType;

import java.util.Set;

public final class BwSpawnLogic {
    private final ServerLevel world;
    private final BwMap map;

    public BwSpawnLogic(ServerLevel world, BwMap map) {
        this.world = world;
        this.map = map;
    }

    public void resetPlayer(ServerPlayer player, GameType gameMode) {
        player.getInventory().clearContent();
        player.getEnderChestInventory().clearContent();

        this.respawnPlayer(player, gameMode);
    }

    public void respawnPlayer(ServerPlayer player, GameType gameMode) {
        player.removeAllEffects();
        player.setHealth(20.0F);
        player.getFoodData().setFoodLevel(20);
        player.fallDistance = 0.0F;
        player.setRemainingFireTicks(0);
        player.setGameMode(gameMode);
    }

    public void spawnAtCenter(ServerPlayer player) {
        Vec3 pos = this.map.getCenterSpawn();
        player.teleportTo(this.world, pos.x, pos.y + 0.5, pos.z, Set.of(), 0.0F, 0.0F, false);
        player.connection.resetPosition();
    }
}
