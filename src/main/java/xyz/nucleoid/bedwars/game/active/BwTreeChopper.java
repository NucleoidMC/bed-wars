package xyz.nucleoid.bedwars.game.active;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.api.util.BlockTraversal;
import xyz.nucleoid.plasmid.api.util.WoodType;

public final class BwTreeChopper {
    public boolean onBreakBlock(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        // Automatic tree breaking
        if (state.isIn(BlockTags.LOGS) && !player.isSneaking()) {
            this.onBreakLog(world, pos);
            return true;
        } else if (state.isIn(BlockTags.LEAVES)) {
            this.onBreakLeaves(world, pos, state);
            return true;
        } else if (state.isOf(Blocks.GOLD_ORE)) {
            this.onBreakOre(player, world, pos, 2, 4, Items.GOLD_INGOT);
        } else if (state.isOf(Blocks.DIAMOND_ORE)) {
            this.onBreakOre(player, world, pos, 1, 2, Items.DIAMOND);
        }

        return false;
    }

    private void onBreakLog(ServerWorld world, BlockPos pos) {
        LongSet logs = this.collectConnectedLogs(world, pos);

        var logPos = new BlockPos.Mutable();

        var logIterator = logs.iterator();
        while (logIterator.hasNext()) {
            logPos.set(logIterator.nextLong());

            BlockState logState = world.getBlockState(logPos);
            world.breakBlock(logPos, false);

            // Drop 1-3 planks
            int count = 1 + world.random.nextInt(3);

            var planks = WoodType.getType(logState.getBlock()).getPlanks();
            world.spawnEntity(new ItemEntity(world, logPos.getX(), logPos.getY(), logPos.getZ(), new ItemStack(planks, count)));
        }
    }

    private void onBreakLeaves(ServerWorld world, BlockPos pos, BlockState state) {
        if (world.random.nextDouble() < 0.025) {
            var plant = WoodType.getType(state.getBlock()).getPlant();
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(plant)));
        }

        if (world.random.nextDouble() < 0.01) {
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.GOLDEN_APPLE)));
        }

        world.removeBlock(pos, false);
    }

    private void onBreakOre(ServerPlayerEntity player, ServerWorld world, BlockPos pos, int minCount, int maxCount, Item drop) {
        world.breakBlock(pos, false);

        int count = minCount + world.random.nextInt(maxCount - minCount + 1);
        ItemStack stack = new ItemStack(drop, count);

        if (!player.getInventory().insertStack(stack.copy())) {
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }

    @NotNull
    private LongSet collectConnectedLogs(ServerWorld world, BlockPos origin) {
        LongSet logs = new LongOpenHashSet();

        BlockTraversal.create()
                .connectivity(BlockTraversal.Connectivity.TWENTY_SIX)
                .accept(origin, (pos, fromPos, depth) -> {
                    var state = world.getBlockState(pos);
                    if (state.isIn(BlockTags.LOGS)) {
                        logs.add(pos.asLong());
                        return BlockTraversal.Result.CONTINUE;
                    } else {
                        return BlockTraversal.Result.TERMINATE;
                    }
                });

        return logs;
    }
}
