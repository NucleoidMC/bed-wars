package net.gegy1000.gl.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class, listeners -> {
        return (player, source) -> {
            for (PlayerDeathCallback listener : listeners) {
                if (listener.onDeath(player, source)) {
                    return true;
                }
            }
            return false;
        };
    });

    boolean onDeath(ServerPlayerEntity player, DamageSource source);
}
