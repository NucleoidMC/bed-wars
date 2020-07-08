package net.gegy1000.bedwars.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public interface CraftCheckCallback {
    Event<CraftCheckCallback> EVENT = EventFactory.createArrayBacked(CraftCheckCallback.class, listeners -> {
        return (world, player, pos) -> {
            for (CraftCheckCallback listener : listeners) {
                if (!listener.canCraft(world, player, pos)) {
                    return false;
                }
            }
            return true;
        };
    });

    boolean canCraft(World world, ServerPlayerEntity player, Recipe<?> recipe);
}
