package net.gegy1000.bedwars.game.map;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public interface MapProvider {
    CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin);
}
