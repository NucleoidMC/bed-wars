package xyz.nucleoid.bedwars.game.active;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.api.util.BlockTraversal;
import xyz.nucleoid.plasmid.api.util.WoodType;

public final class BwTreeChopper {
    public boolean onBreakBlock(ServerPlayer player, ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        // Automatic tree breaking
        if (state.is(BlockTags.LOGS) && !player.isShiftKeyDown()) {
            this.onBreakLog(world, pos);
            return true;
        } else if (state.is(BlockTags.LEAVES)) {
            this.onBreakLeaves(world, pos, state);
            return true;
        } else if (state.is(Blocks.GOLD_ORE)) {
            this.onBreakOre(player, world, pos, 2, 4, Items.GOLD_INGOT);
        } else if (state.is(Blocks.DIAMOND_ORE)) {
            this.onBreakOre(player, world, pos, 1, 2, Items.DIAMOND);
        }

        return false;
    }

    private void onBreakLog(ServerLevel world, BlockPos pos) {
        LongSet logs = this.collectConnectedLogs(world, pos);

        var logPos = new BlockPos.MutableBlockPos();

        var logIterator = logs.iterator();
        while (logIterator.hasNext()) {
            logPos.set(logIterator.nextLong());

            BlockState logState = world.getBlockState(logPos);
            world.destroyBlock(logPos, false);

            // Drop 1-3 planks
            int count = 1 + world.random.nextInt(3);

            var planks = WoodType.getType(logState.getBlock()).getPlanks();
            world.addFreshEntity(new ItemEntity(world, logPos.getX(), logPos.getY(), logPos.getZ(), new ItemStack(planks, count)));
        }
    }

    private void onBreakLeaves(ServerLevel world, BlockPos pos, BlockState state) {
        if (world.random.nextDouble() < 0.025) {
            var plant = WoodType.getType(state.getBlock()).getPlant();
            world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(plant)));
        }

        if (world.random.nextDouble() < 0.01) {
            world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.GOLDEN_APPLE)));
        }

        world.removeBlock(pos, false);
    }

    private void onBreakOre(ServerPlayer player, ServerLevel world, BlockPos pos, int minCount, int maxCount, Item drop) {
        world.destroyBlock(pos, false);

        int count = minCount + world.random.nextInt(maxCount - minCount + 1);
        ItemStack stack = new ItemStack(drop, count);

        if (!player.getInventory().add(stack.copy())) {
            world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }

    @NotNull
    private LongSet collectConnectedLogs(ServerLevel world, BlockPos origin) {
        LongSet logs = new LongOpenHashSet();

        BlockTraversal.create()
                .connectivity(BlockTraversal.Connectivity.TWENTY_SIX)
                .accept(origin, (pos, fromPos, depth) -> {
                    var state = world.getBlockState(pos);
                    if (state.is(BlockTags.LOGS)) {
                        logs.add(pos.asLong());
                        return BlockTraversal.Result.CONTINUE;
                    } else {
                        return BlockTraversal.Result.TERMINATE;
                    }
                });

        return logs;
    }
}
