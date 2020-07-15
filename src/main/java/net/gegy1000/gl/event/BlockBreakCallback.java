package net.gegy1000.gl.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface BlockBreakCallback {
    Event<BlockBreakCallback> EVENT = EventFactory.createArrayBacked(BlockBreakCallback.class, listeners -> {
        return (world, player, pos) -> {
            for (BlockBreakCallback listener : listeners) {
                if (listener.onBreak(world, player, pos)) {
                    return true;
                }
            }
            return false;
        };
    });

    boolean onBreak(ServerWorld world, ServerPlayerEntity player, BlockPos pos);
}
