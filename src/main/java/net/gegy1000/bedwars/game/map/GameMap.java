package net.gegy1000.bedwars.game.map;

import net.gegy1000.bedwars.BlockBounds;
import net.gegy1000.bedwars.game.GameRegion;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class GameMap {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();

    private final ServerWorld world;
    private final BlockBounds bounds;
    private final List<GameRegion> regions;

    private final Set<BlockPos> standardBlocks;

    GameMap(
            ServerWorld world,
            BlockBounds bounds,
            List<GameRegion> regions,
            Set<BlockPos> standardBlocks
    ) {
        this.world = world;
        this.bounds = bounds;
        this.regions = regions;
        this.standardBlocks = standardBlocks;
    }

    public BlockBounds getBounds() {
        return this.bounds;
    }

    public List<GameRegion> getRegions() {
        return this.regions;
    }

    public boolean isStandardBlock(BlockPos pos) {
        return this.standardBlocks.contains(pos);
    }

    public CompletableFuture<Void> delete() {
        return this.world.getServer().submit(() -> {
            ChunkPos minChunk = new ChunkPos(this.bounds.getMin());
            ChunkPos maxChunk = new ChunkPos(this.bounds.getMax());

            for (int z = minChunk.z; z <= maxChunk.z; z++) {
                for (int x = minChunk.x; x <= maxChunk.x; x++) {
                    this.world.getChunk(x, z);
                }
            }

            this.bounds.iterate().forEach(pos -> {
                this.world.setBlockState(pos, AIR, 3);
            });

            List<Entity> entities = this.world.getEntities(null, this.bounds.toBox());
            for (Entity entity : entities) {
                if (!(entity instanceof PlayerEntity)) {
                    entity.remove();
                }
            }
        });
    }
}
