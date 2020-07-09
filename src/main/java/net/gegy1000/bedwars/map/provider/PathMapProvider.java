package net.gegy1000.bedwars.map.provider;

import net.gegy1000.bedwars.map.GameMap;
import net.gegy1000.bedwars.map.GameMapData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public final class PathMapProvider implements MapProvider {
    private final Identifier path;

    public PathMapProvider(Identifier path) {
        this.path = path;
    }

    @Override
    public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin) {
        MinecraftServer server = world.getServer();
        return GameMapData.load(this.path).thenApplyAsync(data -> data.addToWorld(world, origin), server);
    }
}
