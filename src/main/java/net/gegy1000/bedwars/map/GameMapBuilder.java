package net.gegy1000.bedwars.map;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.gegy1000.bedwars.util.BlockBounds;
import net.gegy1000.bedwars.game.GameRegion;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class GameMapBuilder {
    private static final int SET_BLOCK_FLAGS = 0b11;

    private final ServerWorld world;
    private final BlockPos origin;

    private BlockBounds bounds;

    private final List<GameRegion> regions = new ArrayList<>();
    private final LongSet standardBlocks = new LongOpenHashSet();

    private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

    public GameMapBuilder(ServerWorld world, BlockPos origin) {
        this.world = world;
        this.origin = origin;
    }

    public void setBounds(BlockBounds bounds) {
        this.bounds = this.localToGlobal(bounds);
    }

    public void setBlockState(BlockPos pos, BlockState state) {
        BlockPos globalPos = this.localToGlobal(pos);

        this.world.setBlockState(globalPos, state, SET_BLOCK_FLAGS);

        long key = globalPos.asLong();
        if (!state.isAir()) {
            this.standardBlocks.add(key);
        } else {
            this.standardBlocks.remove(key);
        }
    }

    public BlockState getBlockState(BlockPos pos) {
        return world.getBlockState(this.localToGlobal(pos));
    }

    public void setBlockEntity(BlockPos pos, BlockEntity blockEntity) {
        BlockPos globalPos = this.localToGlobal(pos);
        blockEntity.setLocation(this.world, globalPos);

        this.world.setBlockEntity(globalPos, blockEntity);
    }

    public void addRegion(String marker, BlockBounds bounds) {
        bounds = this.localToGlobal(bounds);
        this.regions.add(new GameRegion(marker, bounds));
    }

    public void addRegion(GameRegion region) {
        this.addRegion(region.getMarker(), region.getBounds());
    }

    private BlockPos localToGlobal(BlockPos pos) {
        return this.mutablePos.set(
                this.origin.getX() + pos.getX(),
                this.origin.getY() + pos.getY(),
                this.origin.getZ() + pos.getZ()
        );
    }

    private BlockBounds localToGlobal(BlockBounds bounds) {
        return new BlockBounds(
                this.localToGlobal(bounds.getMin()).toImmutable(),
                this.localToGlobal(bounds.getMax()).toImmutable()
        );
    }

    public GameMap build() {
        if (this.bounds == null) {
            this.bounds = this.computeEncompassingBounds();
        }

        return new GameMap(this.world, this.bounds, this.regions, this.standardBlocks);
    }

    private BlockBounds computeEncompassingBounds() {
        if (this.standardBlocks.isEmpty()) {
            return BlockBounds.EMPTY;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        LongIterator iterator = this.standardBlocks.iterator();
        while (iterator.hasNext()) {
            long pos = iterator.nextLong();

            int x = BlockPos.unpackLongX(pos);
            int y = BlockPos.unpackLongY(pos);
            int z = BlockPos.unpackLongZ(pos);

            if (x < minX) minX = x;
            if (x > maxX) maxX = x;

            if (y < minY) minY = y;
            if (y > maxY) maxY = y;

            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }

        return new BlockBounds(
                new BlockPos(minX, minY, minZ),
                new BlockPos(maxX, maxY, maxZ)
        );
    }
}
