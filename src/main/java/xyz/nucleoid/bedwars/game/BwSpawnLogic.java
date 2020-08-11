package xyz.nucleoid.bedwars.game;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.chunk.ChunkStatus;

public final class BwSpawnLogic {
    private final ServerWorld world;
    private final BwMap map;

    public BwSpawnLogic(ServerWorld world, BwMap map) {
        this.world = world;
        this.map = map;
    }

    // TODO: resetting inventory should be handled by the Game instance
    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.inventory.clear();
        player.getEnderChestInventory().clear();

        this.respawnPlayer(player, gameMode);
    }

    public void respawnPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.clearStatusEffects();
        player.setHealth(20.0F);
        player.getHungerManager().setFoodLevel(20);
        player.fallDistance = 0.0F;
        player.setGameMode(gameMode);
    }

    public void spawnAtCenter(ServerPlayerEntity player) {
        BlockPos pos = this.map.getCenterSpawn();

        ChunkPos chunkPos = new ChunkPos(pos);
        this.world.getChunkManager().addTicket(ChunkTicketType.field_19347, chunkPos, 1, player.getEntityId());

        this.world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL);

        player.teleport(this.world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
    }
}
