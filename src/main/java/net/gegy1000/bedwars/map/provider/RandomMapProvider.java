package net.gegy1000.bedwars.map.provider;

import net.gegy1000.bedwars.map.GameMap;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CompletableFuture;

public final class RandomMapProvider implements MapProvider {
    private final MapProvider[] providers;

    public RandomMapProvider(MapProvider... providers) {
        this.providers = providers;
    }

    @Override
    public CompletableFuture<GameMap> createAt(ServerWorld world, BlockPos origin) {
        int index = world.random.nextInt(this.providers.length);
        MapProvider provider = this.providers[index];
        return provider.createAt(world, origin);
    }
}
